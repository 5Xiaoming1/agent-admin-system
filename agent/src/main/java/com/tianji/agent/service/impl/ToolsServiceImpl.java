package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianji.agent.config.common.OperationLogUtil;
import com.tianji.agent.mapper.ToolsMapper;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.dto.ToolsDTO;
import com.tianji.agent.model.entity.Tools;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.ToolsVO;
import com.tianji.agent.service.ToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具管理服务实现
 * <p>
 * 实现 {@link ToolsService} 接口，完成工具管理的完整业务流程：
 * 分页查询、按 ID 查询、新增、修改、删除、状态切换和全部查询。
 * </p>
 *
 * <p><b>代码存储说明：</b></p>
 * <ul>
 *   <li>工具代码不再存储在 MySQL 中，而是以 .txt 文件形式存储在 data/tools 目录下</li>
 *   <li>文件按类型分目录：data/tools/{type}/{id}.txt</li>
 *   <li>数据库 code_path 字段存储相对路径，如 api/1.txt</li>
 *   <li>查询时从文件读取代码内容，新增/修改时写入文件</li>
 * </ul>
 */
@Service
@Slf4j
public class ToolsServiceImpl implements ToolsService {

    /** 工具管理持久层 Mapper */
    private final ToolsMapper toolsMapper;

    /** 工具代码文件存储根目录（从 application.yaml 注入，默认 data/tools） */
    private final Path toolsBaseDir;

    /** JSON 序列化工具 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ToolsServiceImpl(ToolsMapper toolsMapper,
                            @Value("${agent.tools.storage-dir:data/tools}") String storageDir) {
        this.toolsMapper = toolsMapper;
        this.toolsBaseDir = Paths.get(storageDir).toAbsolutePath().normalize();
        ensureDirectoryExists(toolsBaseDir);
        log.info("工具代码存储目录: {}", toolsBaseDir);
    }

    /**
     * 将前端状态字符串转换为数据库存储的整数值
     */
    private static Integer statusToInt(String status) {
        if (status == null) {
            return null;
        }
        return "active".equals(status) ? 1 : 0;
    }

    // ==================== 文件操作 ====================

    /**
     * 构建工具代码文件路径
     *
     * @param type 工具类型（作为子目录）
     * @param id   工具 ID
     * @return 代码文件完整路径
     */
    private Path buildCodeFilePath(String type, Long id) {
        return toolsBaseDir.resolve(type).resolve(id + ".txt");
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
     * 将代码内容写入文件（生成带@Tool和@P注解的LangChain4j方法）
     *
     * @param type                 工具类型（作为子目录）
     * @param id                   工具 ID
     * @param name                 工具名称
     * @param description          工具描述
     * @param content              方法代码内容
     * @param parameterDescriptions 参数说明
     * @return 相对路径，如 api/1.txt
     */
    private String writeCodeToFile(String type, Long id, String name, String description, 
                                   String content, Map<String, String> parameterDescriptions) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            Path filePath = buildCodeFilePath(type, id);
            ensureDirectoryExists(filePath.getParent());
            
            String annotatedMethod = generateAnnotatedMethod(name, description, content, parameterDescriptions);
            Files.writeString(filePath, annotatedMethod, StandardCharsets.UTF_8);
            String relativePath = type + "/" + id + ".txt";
            log.info("工具代码写入文件: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("写入工具代码文件失败: " + type + "/" + id + ".txt", e);
        }
    }

    /**
     * 生成带@Tool和@P注解的LangChain4j方法代码
     */
    private String generateAnnotatedMethod(String name, String description, String methodCode, 
                                           Map<String, String> parameterDescriptions) {
        StringBuilder sb = new StringBuilder();
        
        // @Tool注解
        sb.append("@Tool(\"").append(description != null ? description : name).append("\")\n");
        
        // 方法代码，自动添加@P注解
        String methodWithAnnotations = addParameterAnnotations(methodCode, parameterDescriptions);
        sb.append(methodWithAnnotations).append("\n");
        
        return sb.toString();
    }

    /**
     * 为方法参数添加@P注解
     */
    private String addParameterAnnotations(String methodCode, Map<String, String> parameterDescriptions) {
        log.info("addParameterAnnotations - parameterDescriptions: {}", parameterDescriptions);
        log.info("addParameterAnnotations - methodCode: {}", methodCode);
        
        if (parameterDescriptions == null || parameterDescriptions.isEmpty()) {
            log.warn("parameterDescriptions 为空，跳过添加@P注解");
            return methodCode;
        }
        
        // 查找方法参数部分 - 使用括号计数匹配，避免方法体内的括号干扰
        int parenStart = methodCode.indexOf('(');
        if (parenStart == -1) {
            log.warn("未找到方法参数起始括号");
            return methodCode;
        }
        
        int parenEnd = findMatchingParen(methodCode, parenStart);
        if (parenEnd == -1) {
            log.warn("未找到匹配的方法参数结束括号");
            return methodCode;
        }
        
        String params = methodCode.substring(parenStart + 1, parenEnd).trim();
        log.info("提取到的参数部分: '{}'", params);
        
        if (params.isEmpty()) {
            return methodCode;
        }
        
        // 分割参数
        String[] paramArray = params.split(",");
        StringBuilder newParams = new StringBuilder();
        
        for (int i = 0; i < paramArray.length; i++) {
            String param = paramArray[i].trim();
            
            // 提取参数名
            String paramName = extractParamName(param);
            log.info("参数[{}]: 原始='{}', 提取的参数名='{}'", i, param, paramName);
            
            String annotation = parameterDescriptions.get(paramName);
            log.info("参数[{}] 查找注解: key='{}', 结果='{}'", i, paramName, annotation);
            
            if (i > 0) {
                newParams.append(", ");
            }
            
            if (annotation != null && !annotation.isEmpty()) {
                newParams.append("@P(\"").append(annotation).append("\") ");
            }
            
            newParams.append(param);
        }
        
        log.info("添加@P注解后的参数: {}", newParams.toString());
        
        return methodCode.substring(0, parenStart + 1) + "\n            " + 
               newParams.toString() + "\n    " + methodCode.substring(parenEnd);
    }

    /**
     * 从指定位置开始，找到匹配的右括号
     */
    private int findMatchingParen(String code, int openParenIndex) {
        int depth = 0;
        for (int i = openParenIndex; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 从参数声明中提取参数名
     */
    private String extractParamName(String param) {
        // 例如: "String city" -> "city"
        String[] parts = param.trim().split("\\s+");
        if (parts.length >= 2) {
            return parts[parts.length - 1];
        }
        return param.trim();
    }

    /**
     * 从文件读取代码内容
     *
     * @param codePath 相对路径，如 api/1.txt
     * @return 代码内容，文件不存在或路径为空时返回空字符串
     */
    private String readCodeFromFile(String codePath) {
        if (codePath == null || codePath.isEmpty()) {
            return "";
        }
        try {
            Path filePath = toolsBaseDir.resolve(codePath);
            if (!Files.exists(filePath)) {
                log.warn("工具代码文件不存在: {}", codePath);
                return "";
            }
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取工具代码文件失败: {}", codePath, e);
            return "";
        }
    }

    /**
     * 删除代码文件
     *
     * @param codePath 相对路径
     */
    private void deleteCodeFile(String codePath) {
        if (codePath == null || codePath.isEmpty()) {
            return;
        }
        try {
            Path filePath = toolsBaseDir.resolve(codePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除工具代码文件: {}", codePath);
            }
        } catch (IOException e) {
            log.warn("删除工具代码文件失败: {}", codePath, e);
        }
    }

    /**
     * 为 VO 填充代码内容（从文件读取）
     */
    private void fillCodeFromFile(ToolsVO vo, Tools entity) {
        if (entity.getCodePath() != null) {
            vo.setCode(readCodeFromFile(entity.getCodePath()));
        }
        if (entity.getParameterDescriptions() != null) {
            vo.setParameterDescriptions(parseParameterDescriptions(entity.getParameterDescriptions()));
        }
        if (entity.getParameters() != null) {
            vo.setParameters(parseParameterDescriptions(entity.getParameters()));
        }
    }

    /**
     * 将 JSON 字符串解析为 Map
     */
    private Map<String, String> parseParameterDescriptions(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析参数说明 JSON 失败: {}", json, e);
            return null;
        }
    }

    /**
     * 将 Map 序列化为 JSON 字符串
     */
    private String serializeParameterDescriptions(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("序列化参数说明失败", e);
            return null;
        }
    }

    // ==================== 业务方法 ====================

    @Override
    public PageVO<ToolsVO> queryPage(Integer page, Integer pageSize, String keyword, String type, String status) {
        log.info("分页查询工具: page={}, pageSize={}, keyword={}, type={}, status={}", page, pageSize, keyword, type, status);

        Page<Tools> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Tools> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Tools::getName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Tools::getType, type);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Tools::getStatus, statusToInt(status));
        }
        queryWrapper.orderByDesc(Tools::getCreateTime);

        Page<Tools> entityPage = toolsMapper.selectPage(pageParam, queryWrapper);
        List<ToolsVO> voList = entityPage.getRecords().stream()
                .map(entity -> {
                    ToolsVO vo = ToolsVO.fromEntity(entity);
                    fillCodeFromFile(vo, entity);
                    return vo;
                })
                .collect(Collectors.toList());

        Page<ToolsVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public ToolsVO queryById(Long id) {
        log.info("按ID查询工具: {}", id);

        Tools entity = toolsMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("工具不存在");
        }
        ToolsVO vo = ToolsVO.fromEntity(entity);
        fillCodeFromFile(vo, entity);
        return vo;
    }

    @Override
    public ToolsVO create(ToolsDTO dto) {
        log.info("新增工具: {}, parameterDescriptions={}", dto.getName(), dto.getParameterDescriptions());

        Tools entity = new Tools();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setParameterDescriptions(serializeParameterDescriptions(dto.getParameterDescriptions()));
        entity.setEndpoint(dto.getEndpoint());
        entity.setParameters(serializeParameterDescriptions(dto.getParameters()));
        entity.setStatus(1);

        toolsMapper.insert(entity);
        log.info("工具记录创建成功, id: {}", entity.getId());

        String codePath = writeCodeToFile(dto.getType(), entity.getId(), dto.getName(), 
                                          dto.getDescription(), dto.getCode(), dto.getParameterDescriptions());
        entity.setCodePath(codePath);
        toolsMapper.updateById(entity);

        // 记录详细的操作日志
        OperationLogUtil.log("工具管理", "新增", 
            String.format("新增工具【%s】，类型：%s", dto.getName(), dto.getType()));

        ToolsVO vo = ToolsVO.fromEntity(entity);
        vo.setCode(dto.getCode());
        return vo;
    }

    @Override
    public ToolsVO update(Long id, ToolsDTO dto) {
        log.info("修改工具: id={}, name={}", id, dto.getName());

        Tools oldEntity = toolsMapper.selectById(id);
        if (oldEntity == null) {
            throw new RuntimeException("工具不存在");
        }

        String oldType = oldEntity.getType();
        String oldCodePath = oldEntity.getCodePath();
        String oldName = oldEntity.getName();

        Tools entity = new Tools();
        entity.setId(oldEntity.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setParameterDescriptions(serializeParameterDescriptions(dto.getParameterDescriptions()));
        entity.setEndpoint(dto.getEndpoint());
        entity.setParameters(serializeParameterDescriptions(dto.getParameters()));
        entity.setCodePath(oldEntity.getCodePath());
        entity.setStatus(oldEntity.getStatus());

        // 如果类型变更，删除旧文件，写入新文件
        if (!oldType.equals(dto.getType()) && oldCodePath != null) {
            deleteCodeFile(oldCodePath);
        }

        String newCodePath = writeCodeToFile(dto.getType(), id, dto.getName(), 
                                             dto.getDescription(), dto.getCode(), dto.getParameterDescriptions());
        entity.setCodePath(newCodePath);

        toolsMapper.updateById(entity);
        log.info("工具修改成功, id: {}", id);

        // 记录详细的操作日志（对比修改前后）
        List<String> changes = new ArrayList<>();
        if (!oldName.equals(dto.getName())) {
            changes.add(String.format("名称: %s -> %s", oldName, dto.getName()));
        }
        if (!oldType.equals(dto.getType())) {
            changes.add(String.format("类型: %s -> %s", oldType, dto.getType()));
        }
        
        ToolsVO vo = ToolsVO.fromEntity(entity);
        vo.setCode(dto.getCode());
        
        if (!changes.isEmpty()) {
            OperationLogUtil.log("工具管理", "修改", 
                String.format("修改工具【%s】，ID：%d | %s", dto.getName(), id, String.join("；", changes)));
        } else {
            OperationLogUtil.log("工具管理", "修改", 
                String.format("修改工具【%s】，ID：%d（无实质变更）", dto.getName(), id));
        }
        
        return vo;
    }

    @Override
    public void delete(Long id) {
        log.info("删除工具: {}", id);

        Tools entity = toolsMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("工具不存在");
        }

        // 记录删除的详细信息
        OperationLogUtil.log("工具管理", "删除", 
            String.format("删除工具【%s】，类型：%s，工具ID：%d", entity.getName(), entity.getType(), id));

        // 先删除代码文件，再删除数据库记录
        deleteCodeFile(entity.getCodePath());
        toolsMapper.deleteById(id);
        log.info("工具删除成功, id: {}", id);
    }

    @Override
    public String toggleStatus(Long id, StatusDTO dto) {
        log.info("切换工具状态: id={}, targetStatus={}", id, dto.getStatus());

        Tools entity = toolsMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("工具不存在");
        }

        String oldStatus = entity.getStatus() == 1 ? "启用" : "禁用";
        String newStatus = "active".equals(dto.getStatus()) ? "启用" : "禁用";

        Integer newStatusInt = statusToInt(dto.getStatus());
        entity.setStatus(newStatusInt);
        toolsMapper.updateById(entity);

        String resultStatus = newStatusInt == 1 ? "active" : "inactive";
        log.info("工具状态切换成功: id={}, status={}", id, resultStatus);
        
        // 记录状态切换的详细日志
        OperationLogUtil.log("工具管理", "切换状态", 
            String.format("切换工具【%s】状态：%s -> %s，工具ID：%d", entity.getName(), oldStatus, newStatus, id));
        
        return resultStatus;
    }

    @Override
    public List<ToolsVO> queryAll() {
        log.info("查询全部工具");

        LambdaQueryWrapper<Tools> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tools::getStatus, 1);
        queryWrapper.orderByDesc(Tools::getCreateTime);

        List<Tools> entities = toolsMapper.selectList(queryWrapper);
        return entities.stream()
                .map(entity -> {
                    ToolsVO vo = ToolsVO.fromEntity(entity);
                    fillCodeFromFile(vo, entity);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}