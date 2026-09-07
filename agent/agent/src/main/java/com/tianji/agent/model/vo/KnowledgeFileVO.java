package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库文件信息响应 VO
 * <p>
 * 封装知识库文件的详细信息，用于文件列表、上传和下载等接口的响应数据。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库文件信息视图对象")
public class KnowledgeFileVO {

    /** 文件 ID（使用文件名或 UUID） */
    @JsonProperty("id")
    @Schema(description = "文件ID")
    private String id;

    /** 存储文件名（带时间戳和随机后缀） */
    @JsonProperty("name")
    @Schema(description = "存储文件名")
    private String name;

    /** 原始文件名（用户上传时的文件名） */
    @JsonProperty("originalName")
    @Schema(description = "原始文件名")
    private String originalName;

    /** 文件大小（字节） */
    @JsonProperty("size")
    @Schema(description = "文件大小（字节）")
    private Long size;

    /** 文件 MIME 类型 */
    @JsonProperty("mimeType")
    @Schema(description = "文件MIME类型")
    private String mimeType;

    /** 文件访问 URL（相对路径） */
    @JsonProperty("url")
    @Schema(description = "文件访问URL")
    private String url;

    /** 文件预览 URL（可选，用于前端预览） */
    @JsonProperty("previewUrl")
    @Schema(description = "文件预览URL")
    private String previewUrl;

    /** 文件最后修改时间 */
    @JsonProperty("lastModified")
    @Schema(description = "最后修改时间")
    private LocalDateTime lastModified;
}