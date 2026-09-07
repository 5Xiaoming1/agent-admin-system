package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.Knowledge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库管理响应 VO
 * <p>
 * 封装知识库管理接口的响应数据，将数据库中的状态值（1/0）转换为前端可识别的
 * 字符串格式（active/inactive），日期字段使用 ISO 8601 格式序列化。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识库信息视图对象")
public class KnowledgeVO {

    /** 知识库 ID */
    @JsonProperty("id")
    @Schema(description = "知识库ID")
    private String id;

    /** 知识库名称 */
    @Schema(description = "知识库名称")
    private String name;

    /** 知识库描述 */
    @Schema(description = "知识库描述")
    private String description;

    /** 知识库类型：document/qa/web */
    @Schema(description = "知识库类型：document-文档，qa-问答，web-网页")
    private String type;

    /** 文档数量 */
    @JsonProperty("documentCount")
    @Schema(description = "文档数量")
    private Integer documentCount;

    /** 状态：active（启用）/ inactive（禁用） */
    @Schema(description = "状态：active-启用，inactive-禁用")
    private String status;

    /** 知识原数据上传文件路径 */
    @JsonProperty("sourceFilePath")
    @Schema(description = "知识原数据上传文件路径")
    private String sourceFilePath;

    /** 创建时间 */
    @JsonProperty("createdAt")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @JsonProperty("updatedAt")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;

    /**
     * 将数据库实体转换为 VO
     * <p>
     * 将数据库中的状态值 1/0 转换为前端可识别的 active/inactive 字符串，
     * 并将 Long 类型的 ID 转为 String 类型。
     * </p>
     *
     * @param entity 数据库实体对象
     * @return 转换后的 VO 对象
     */
    public static KnowledgeVO fromEntity(Knowledge entity) {
        return KnowledgeVO.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .documentCount(entity.getDocumentCount() != null ? entity.getDocumentCount() : 0)
                .status(entity.getStatus() == 1 ? "active" : "inactive")
                .sourceFilePath(entity.getSourceFilePath())
                .createdAt(entity.getCreateTime())
                .updatedAt(entity.getUpdateTime())
                .build();
    }
}