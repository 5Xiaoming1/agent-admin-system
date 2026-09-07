package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.config.common.OperationLogUtil;
import com.tianji.agent.config.upload.FileUploadUtil;
import com.tianji.agent.mapper.KnowledgeMapper;
import com.tianji.agent.model.dto.KnowledgeDTO;
import com.tianji.agent.model.entity.Knowledge;
import com.tianji.agent.model.vo.KnowledgeFileVO;
import com.tianji.agent.model.vo.KnowledgeVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 知识库管理服务实现
 * <p>
 * 实现 {@link KnowledgeService} 接口，完成知识库管理的完整业务流程：
 * 分页查询、按 ID 查询、新增、修改、删除和全部查询。
 * </p>
 *
 * <p><b>文件存储说明：</b></p>
 * <ul>
 *   <li>知识库实际文档内容以文件形式存储在 data/knowledgebase 目录下</li>
 *   <li>文件按类型分目录：data/knowledgebase/{type}/{id}/</li>
 *   <li>数据库 content_path 字段存储相对路径，如 document/1/</li>
 *   <li>document_count 字段自动统计目录下的文件数量</li>
 * </ul>
 */
@Service
@Slf4j
public class KnowledgeServiceImpl implements KnowledgeService {

    /** 知识库管理持久层 Mapper */
    private final KnowledgeMapper knowledgeMapper;

    /** 知识库文件存储根目录（从 application.yaml 注入，默认 data/knowledgebase） */
    private final Path knowledgeBaseDir;

    public KnowledgeServiceImpl(KnowledgeMapper knowledgeMapper,
                                @Value("${agent.knowledgebase.storage-dir:data/knowledgebase}") String storageDir) {
        this.knowledgeMapper = knowledgeMapper;
        this.knowledgeBaseDir = Paths.get(storageDir).toAbsolutePath().normalize();
        ensureDirectoryExists(knowledgeBaseDir);
        log.info("知识库文件存储目录: {}", knowledgeBaseDir);
    }

    // ==================== 文件操作 ====================

    /**
     * 构建知识库内容存储目录路径
     *
     * @param type 知识库类型（作为子目录）
     * @param id   知识库 ID
     * @return 内容目录完整路径
     */
    private Path buildContentDir(String type, Long id) {
        return knowledgeBaseDir.resolve(type).resolve(String.valueOf(id));
    }

    /**
     * 确保目录存在，不存在则创建
     */
    private void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("创建目录: {}", dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建目录: " + dir, e);
        }
    }

    /**
     * 创建知识库内容目录
     *
     * @param type 知识库类型
     * @param id   知识库 ID
     * @return 相对路径，如 document/1/
     */
    private String createContentDir(String type, Long id) {
        try {
            Path dir = buildContentDir(type, id);
            ensureDirectoryExists(dir);
            String relativePath = type + "/" + id + "/";
            log.info("知识库内容目录创建成功: {}", relativePath);
            return relativePath;
        } catch (RuntimeException e) {
            throw new RuntimeException("创建知识库内容目录失败: " + type + "/" + id + "/", e);
        }
    }

    /**
     * 统计目录下的文件数量
     *
     * @param contentPath 相对路径，如 document/1/
     * @return 文件数量，目录不存在或路径为空时返回 0
     */
    private int countDocuments(String contentPath) {
        if (contentPath == null || contentPath.isEmpty()) {
            return 0;
        }
        try {
            Path dir = knowledgeBaseDir.resolve(contentPath);
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                return 0;
            }
            try (Stream<Path> files = Files.list(dir)) {
                return (int) files.filter(Files::isRegularFile).count();
            }
        } catch (IOException e) {
            log.warn("统计知识库文档数量失败: {}", contentPath, e);
            return 0;
        }
    }

    /**
     * 删除知识库内容目录
     *
     * @param contentPath 相对路径
     */
    private void deleteContentDir(String contentPath) {
        if (contentPath == null || contentPath.isEmpty()) {
            return;
        }
        try {
            Path dir = knowledgeBaseDir.resolve(contentPath);
            if (Files.exists(dir)) {
                try (Stream<Path> files = Files.walk(dir)) {
                    files.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    log.warn("删除文件失败: {}", path, e);
                                }
                            });
                }
                log.info("删除知识库内容目录: {}", contentPath);
            }
        } catch (IOException e) {
            log.warn("删除知识库内容目录失败: {}", contentPath, e);
        }
    }

    /**
     * 迁移内容目录（当类型变更时）
     *
     * @param oldPath 旧路径
     * @param newType 新类型
     * @param id      知识库 ID
     * @return 新路径
     */
    private String migrateContentDir(String oldPath, String newType, Long id) {
        if (oldPath == null || oldPath.isEmpty()) {
            return createContentDir(newType, id);
        }
        try {
            Path oldDir = knowledgeBaseDir.resolve(oldPath);
            Path newDir = buildContentDir(newType, id);
            if (Files.exists(oldDir) && !oldDir.equals(newDir)) {
                ensureDirectoryExists(newDir.getParent());
                Files.move(oldDir, newDir);
                log.info("知识库内容目录迁移: {} -> {}", oldPath, newType + "/" + id + "/");
            } else if (!Files.exists(newDir)) {
                ensureDirectoryExists(newDir);
            }
            return newType + "/" + id + "/";
        } catch (IOException e) {
            log.warn("迁移知识库内容目录失败，将创建新目录: {}", e.getMessage());
            return createContentDir(newType, id);
        }
    }

    // ==================== 业务方法 ====================

    @Override
    public PageVO<KnowledgeVO> queryPage(Integer page, Integer pageSize, String keyword, String type) {
        log.info("分页查询知识库: page={}, pageSize={}, keyword={}, type={}", page, pageSize, keyword, type);

        Page<Knowledge> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Knowledge> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Knowledge::getName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Knowledge::getType, type);
        }
        queryWrapper.orderByDesc(Knowledge::getCreateTime);

        Page<Knowledge> entityPage = knowledgeMapper.selectPage(pageParam, queryWrapper);
        List<KnowledgeVO> voList = entityPage.getRecords().stream()
                .map(entity -> {
                    KnowledgeVO vo = KnowledgeVO.fromEntity(entity);
                    vo.setDocumentCount(countDocuments(entity.getContentPath()));
                    return vo;
                })
                .collect(Collectors.toList());

        Page<KnowledgeVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public KnowledgeVO queryById(Long id) {
        log.info("按ID查询知识库: {}", id);

        Knowledge entity = knowledgeMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }
        KnowledgeVO vo = KnowledgeVO.fromEntity(entity);
        vo.setDocumentCount(countDocuments(entity.getContentPath()));
        return vo;
    }

    @Override
    public KnowledgeVO create(String name, String description, String type, MultipartFile file) {
        log.info("新增知识库: name={}, type={}", name, type);

        Knowledge entity = new Knowledge();
        entity.setName(name);
        entity.setDescription(description);
        entity.setType(type);
        entity.setDocumentCount(0);
        entity.setStatus(1);

        knowledgeMapper.insert(entity);
        log.info("知识库记录创建成功, id: {}", entity.getId());

        String contentPath = createContentDir(type, entity.getId());
        entity.setContentPath(contentPath);

        // 如果有上传文件，保存到知识库目录
        if (file != null && !file.isEmpty()) {
            try {
                Path targetPath = buildContentDir(type, entity.getId()).resolve(file.getOriginalFilename());
                file.transferTo(targetPath.toFile());
                log.info("文件上传成功: {}", file.getOriginalFilename());
                entity.setDocumentCount(1);
            } catch (IOException e) {
                log.error("文件保存失败: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("文件保存失败: " + e.getMessage());
            }
        }

        knowledgeMapper.updateById(entity);

        // 记录详细的操作日志
        String fileMsg = (file != null && !file.isEmpty()) ? "，上传文件：" + file.getOriginalFilename() : "";
        OperationLogUtil.log("知识库管理", "新增", 
            String.format("新增知识库【%s】，类型：%s%s", name, type, fileMsg));

        return KnowledgeVO.fromEntity(entity);
    }

    @Override
    public KnowledgeVO update(Long id, KnowledgeDTO dto) {
        log.info("修改知识库: id={}, name={}", id, dto.getName());

        Knowledge oldEntity = knowledgeMapper.selectById(id);
        if (oldEntity == null) {
            throw new RuntimeException("知识库不存在");
        }

        String oldType = oldEntity.getType();
        String oldContentPath = oldEntity.getContentPath();
        String oldName = oldEntity.getName();

        Knowledge entity = new Knowledge();
        entity.setId(oldEntity.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setContentPath(oldEntity.getContentPath());
        entity.setDocumentCount(oldEntity.getDocumentCount());
        entity.setStatus(oldEntity.getStatus());

        // 如果类型变更，迁移文件目录
        if (!oldType.equals(dto.getType())) {
            String newContentPath = migrateContentDir(oldContentPath, dto.getType(), id);
            entity.setContentPath(newContentPath);
        }

        knowledgeMapper.updateById(entity);
        log.info("知识库修改成功, id: {}", id);

        // 记录详细的操作日志（对比修改前后）
        List<String> changes = new ArrayList<>();
        if (!oldName.equals(dto.getName())) {
            changes.add(String.format("名称: %s -> %s", oldName, dto.getName()));
        }
        if (!oldType.equals(dto.getType())) {
            changes.add(String.format("类型: %s -> %s", oldType, dto.getType()));
        }
        
        KnowledgeVO vo = KnowledgeVO.fromEntity(entity);
        vo.setDocumentCount(countDocuments(entity.getContentPath()));
        
        if (!changes.isEmpty()) {
            OperationLogUtil.log("知识库管理", "修改", 
                String.format("修改知识库【%s】，ID：%d | %s", dto.getName(), id, String.join("；", changes)));
        } else {
            OperationLogUtil.log("知识库管理", "修改", 
                String.format("修改知识库【%s】，ID：%d（无实质变更）", dto.getName(), id));
        }
        
        return vo;
    }

    @Override
    public void delete(Long id) {
        log.info("删除知识库: {}", id);

        Knowledge entity = knowledgeMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }

        // 记录删除的详细信息
        OperationLogUtil.log("知识库管理", "删除", 
            String.format("删除知识库【%s】，类型：%s，文档数：%d，知识库ID：%d", 
                entity.getName(), entity.getType(), countDocuments(entity.getContentPath()), id));

        // 先删除内容目录，再删除数据库记录
        deleteContentDir(entity.getContentPath());
        knowledgeMapper.deleteById(id);
        log.info("知识库删除成功, id: {}", id);
    }

    @Override
    public List<KnowledgeVO> queryAll() {
        log.info("查询全部知识库");

        LambdaQueryWrapper<Knowledge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Knowledge::getStatus, 1);
        queryWrapper.orderByDesc(Knowledge::getCreateTime);

        List<Knowledge> entities = knowledgeMapper.selectList(queryWrapper);
        return entities.stream()
                .map(entity -> {
                    KnowledgeVO vo = KnowledgeVO.fromEntity(entity);
                    vo.setDocumentCount(countDocuments(entity.getContentPath()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ==================== 文件管理方法 ====================

    @Override
    public List<KnowledgeFileVO> listFiles(Long knowledgeId) {
        log.info("获取知识库文件列表: knowledgeId={}", knowledgeId);

        Knowledge entity = knowledgeMapper.selectById(knowledgeId);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }

        String contentPath = entity.getContentPath();
        if (contentPath == null || contentPath.isEmpty()) {
            return new ArrayList<>();
        }

        Path dir = knowledgeBaseDir.resolve(contentPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return new ArrayList<>();
        }

        List<KnowledgeFileVO> fileList = new ArrayList<>();
        try (Stream<Path> files = Files.list(dir)) {
            fileList = files
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::getFileLastModifiedTime))
                    .map(path -> buildFileVO(path, contentPath))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("获取知识库文件列表失败: {}", contentPath, e);
            throw new RuntimeException("获取文件列表失败: " + e.getMessage());
        }

        log.info("获取到 {} 个文件", fileList.size());
        return fileList;
    }

    @Override
    public KnowledgeFileVO uploadFile(Long knowledgeId, MultipartFile file) {
        log.info("上传文件到知识库: knowledgeId={}, fileName={}", knowledgeId, file.getOriginalFilename());

        Knowledge entity = knowledgeMapper.selectById(knowledgeId);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }

        String contentPath = entity.getContentPath();
        if (contentPath == null || contentPath.isEmpty()) {
            throw new RuntimeException("知识库内容路径不存在");
        }

        Path dir = knowledgeBaseDir.resolve(contentPath);
        FileUploadUtil.ensureDirectoryExists(dir);

        String originalFilename = file.getOriginalFilename();
        String storedFilename = FileUploadUtil.generateKnowledgeFilename(originalFilename);
        Path targetPath = dir.resolve(storedFilename);

        FileUploadUtil.saveFile(file, targetPath);

        entity.setDocumentCount(countDocuments(contentPath));
        knowledgeMapper.updateById(entity);

        // 记录详细的操作日志
        OperationLogUtil.log("知识库管理", "上传文件", 
            String.format("上传文件【%s】到知识库【%s】，知识库ID：%d", originalFilename, entity.getName(), knowledgeId));

        return buildFileVO(targetPath, contentPath);
    }

    @Override
    public void deleteFile(Long knowledgeId, String fileId) {
        log.info("删除知识库文件: knowledgeId={}, fileId={}", knowledgeId, fileId);

        Knowledge entity = knowledgeMapper.selectById(knowledgeId);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }

        String contentPath = entity.getContentPath();
        if (contentPath == null || contentPath.isEmpty()) {
            throw new RuntimeException("知识库内容路径不存在");
        }

        Path filePath = knowledgeBaseDir.resolve(contentPath).resolve(fileId);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }

        try {
            Files.delete(filePath);
            log.info("文件删除成功: {}", fileId);

            // 记录详细的操作日志
            OperationLogUtil.log("知识库管理", "删除文件", 
                String.format("从知识库【%s】删除文件【%s】，知识库ID：%d", entity.getName(), fileId, knowledgeId));

            entity.setDocumentCount(countDocuments(contentPath));
            knowledgeMapper.updateById(entity);
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileId, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public Resource getFileResource(Long knowledgeId, String fileId) {
        log.info("获取知识库文件资源（下载）: knowledgeId={}, fileId={}", knowledgeId, fileId);

        Path filePath = resolveFilePath(knowledgeId, fileId);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("文件不存在或不可读");
            }
            return resource;
        } catch (MalformedURLException e) {
            log.error("获取文件资源失败: {}", fileId, e);
            throw new RuntimeException("获取文件资源失败: " + e.getMessage());
        }
    }

    // ==================== 文件操作辅助方法 ====================

    /**
     * 解析文件完整路径
     *
     * @param knowledgeId 知识库 ID
     * @param fileId      文件 ID（存储文件名）
     * @return 文件完整路径
     */
    private Path resolveFilePath(Long knowledgeId, String fileId) {
        Knowledge entity = knowledgeMapper.selectById(knowledgeId);
        if (entity == null) {
            throw new RuntimeException("知识库不存在");
        }

        String contentPath = entity.getContentPath();
        if (contentPath == null || contentPath.isEmpty()) {
            throw new RuntimeException("知识库内容路径不存在");
        }

        Path filePath = knowledgeBaseDir.resolve(contentPath).resolve(fileId);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }

        return filePath;
    }

    /**
     * 获取文件最后修改时间
     *
     * @param path 文件路径
     * @return 文件最后修改时间
     */
    private LocalDateTime getFileLastModifiedTime(Path path) {
        try {
            return LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(),
                    java.time.ZoneId.systemDefault()
            );
        } catch (IOException e) {
            return LocalDateTime.now();
        }
    }

    /**
     * 构建文件 VO 对象
     *
     * @param path        文件路径
     * @param contentPath 知识库内容相对路径
     * @return 文件 VO 对象
     */
    private KnowledgeFileVO buildFileVO(Path path, String contentPath) {
        try {
            String filename = path.getFileName().toString();
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return KnowledgeFileVO.builder()
                    .id(filename)
                    .name(filename)
                    .originalName(filename)
                    .size(Files.size(path))
                    .mimeType(mimeType)
                    .url("/storage/knowledge/" + contentPath + filename)
                    .previewUrl("/storage/knowledge/" + contentPath + filename + "?preview=true")
                    .lastModified(getFileLastModifiedTime(path))
                    .build();
        } catch (IOException e) {
            log.warn("构建文件VO失败: {}", path, e);
            return null;
        }
    }
}