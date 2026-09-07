package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.dto.KnowledgeDTO;
import com.tianji.agent.model.vo.KnowledgeFileVO;
import com.tianji.agent.model.vo.KnowledgeVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 知识库管理控制器
 * <p>
 * 处理知识库管理的 HTTP 请求，作为 Controller 层负责：
 * <ul>
 *   <li>分页查询知识库列表（支持关键词搜索、类型筛选）</li>
 *   <li>按 ID 查询知识库详情</li>
 *   <li>新增知识库（自动创建文件存储目录）</li>
 *   <li>修改知识库（类型变更时自动迁移目录）</li>
 *   <li>删除知识库（逻辑删除 + 删除文件存储目录）</li>
 *   <li>查询全部已启用知识库</li>
 *   <li>获取知识库文件列表</li>
 *   <li>上传文件到知识库</li>
 *   <li>删除知识库文件</li>
 *   <li>下载知识库文件</li>
 * </ul>
 * 知识库的实际文档内容以文件形式存储在 data/knowledgebase 目录下，按类型分目录组织。
 * </p>
 */
@RestController
@RequestMapping("/api/knowledgebase")
@Tag(name = "知识库管理", description = "知识库管理相关接口")
@Slf4j
@AllArgsConstructor
public class KnowledgeController {

    /** 知识库管理服务 */
    private final KnowledgeService knowledgeService;

    /**
     * 分页查询知识库列表
     * <p>
     * 支持按知识库名称关键词模糊搜索、按类型筛选，可单独或组合使用。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  知识库名称关键词（可选）
     * @param type     知识库类型筛选（可选），document/qa/web
     * @return 统一响应结果，data 包含知识库列表和分页信息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询知识库列表", description = "支持按名称模糊搜索、按类型筛选")
    public Result<PageVO<KnowledgeVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        PageVO<KnowledgeVO> result = knowledgeService.queryPage(page, pageSize, keyword, type);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询知识库
     *
     * @param id 知识库 ID
     * @return 统一响应结果，data 包含知识库详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询知识库", description = "返回知识库详细信息")
    public Result<KnowledgeVO> getById(@PathVariable Long id) {
        KnowledgeVO result = knowledgeService.queryById(id);
        return Result.success(result);
    }

    /**
     * 新增知识库
     * <p>
     * 创建知识库记录并自动创建文件存储目录，支持上传初始文档。
     * </p>
     *
     * @param name        知识库名称
     * @param description 知识库描述
     * @param type        知识库类型：document/qa/web
     * @param file        初始文档文件（可选）
     * @return 统一响应结果，data 包含新建的知识库信息
     */
    @PostMapping("/create")
    @Operation(summary = "新增知识库", description = "创建知识库记录并自动创建文件存储目录，支持上传初始文档")
    public Result<KnowledgeVO> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file) {
        KnowledgeVO result = knowledgeService.create(name, description, type, file);
        return Result.success(result);
    }

    /**
     * 修改知识库
     * <p>
     * 更新知识库基本信息，类型变更时自动迁移文件存储目录。
     * </p>
     *
     * @param id  知识库 ID
     * @param dto 知识库请求参数
     * @return 统一响应结果，data 包含更新后的知识库信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改知识库", description = "更新知识库基本信息，类型变更时自动迁移文件存储目录")
    public Result<KnowledgeVO> update(@PathVariable Long id, @Valid @RequestBody KnowledgeDTO dto) {
        KnowledgeVO result = knowledgeService.update(id, dto);
        return Result.success(result);
    }

    /**
     * 删除知识库
     * <p>
     * 逻辑删除知识库记录，同时删除对应的文件存储目录。
     * </p>
     *
     * @param id 知识库 ID
     * @return 统一响应结果
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除知识库", description = "逻辑删除知识库记录，同时删除文件存储目录")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 查询全部知识库
     * <p>
     * 不分页，仅返回已启用的知识库，供下拉选择等场景使用。
     * </p>
     *
     * @return 统一响应结果，data 包含已启用的知识库列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取全部知识库", description = "获取全部已启用的知识库，供下拉选择等场景使用")
    public Result<List<KnowledgeVO>> all() {
        List<KnowledgeVO> result = knowledgeService.queryAll();
        return Result.success(result);
    }

    /**
     * 获取知识库文件列表
     * <p>
     * 查询指定知识库目录下的所有文件信息，包括文件名、大小、MIME 类型等。
     * </p>
     *
     * @param id 知识库 ID（路径参数）
     * @return 统一响应结果，data 包含文件信息列表
     */
    @GetMapping("/{id}/files")
    @Operation(summary = "获取知识库文件列表", description = "查询指定知识库目录下的所有文件信息")
    public Result<List<KnowledgeFileVO>> listFiles(@PathVariable Long id) {
        List<KnowledgeFileVO> result = knowledgeService.listFiles(id);
        return Result.success(result);
    }

    /**
     * 上传文件到知识库
     * <p>
     * 将文件上传到指定知识库的存储目录，自动生成带时间戳和随机后缀的文件名，
     * 并更新知识库的文档数量。
     * </p>
     *
     * @param id   知识库 ID（路径参数）
     * @param file 要上传的文件
     * @return 统一响应结果，data 包含上传后的文件信息
     */
    @PostMapping("/{id}/files/upload")
    @Operation(summary = "上传文件到知识库", description = "将文件上传到指定知识库的存储目录")
    public Result<KnowledgeFileVO> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        KnowledgeFileVO result = knowledgeService.uploadFile(id, file);
        return Result.success("上传成功", result);
    }

    /**
     * 删除知识库文件
     * <p>
     * 从指定知识库的存储目录中删除文件，并更新知识库的文档数量。
     * </p>
     *
     * @param id       知识库 ID（路径参数）
     * @param fileName 文件名（路径参数，存储文件名）
     * @return 统一响应结果，删除成功
     */
    @DeleteMapping("/{id}/files/{fileName}")
    @Operation(summary = "删除知识库文件", description = "从指定知识库的存储目录中删除文件")
    public Result<Void> deleteFile(@PathVariable Long id, @PathVariable String fileName) {
        knowledgeService.deleteFile(id, fileName);
        return Result.success("删除成功", null);
    }

    /**
     * 下载知识库文件
     * <p>
     * 返回指定知识库文件的二进制流，浏览器自动触发下载。
     * 响应头包含 Content-Type 和 Content-Disposition。
     * </p>
     *
     * @param id       知识库 ID（路径参数）
     * @param fileName 文件名（路径参数，存储文件名）
     * @return 文件二进制流
     */
    @GetMapping("/{id}/files/{fileName}/download")
    @Operation(summary = "下载知识库文件", description = "返回指定知识库文件的二进制流，浏览器自动触发下载")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @PathVariable String fileName) {
        Resource resource = knowledgeService.getFileResource(id, fileName);
        String mimeType = resource.getFilename() != null ? getMimeType(resource.getFilename()) : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodeFilename(fileName) + "\"")
                .body(resource);
    }

    /**
     * 根据文件名获取 MIME 类型
     *
     * @param filename 文件名
     * @return MIME 类型
     */
    private String getMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".md")) {
            return "text/markdown";
        } else if (lowerName.endsWith(".csv")) {
            return "text/csv";
        } else if (lowerName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "application/octet-stream";
    }

    /**
     * URL 编码文件名（处理中文文件名）
     *
     * @param filename 文件名
     * @return 编码后的文件名
     */
    private String encodeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }
}