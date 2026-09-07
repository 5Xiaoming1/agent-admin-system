package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库管理请求 DTO
 * <p>
 * 用于新增和修改知识库时接收前端提交的请求参数，
 * 包含知识库名称、描述和类型。
 * </p>
 */
@Data
@Schema(description = "知识库管理请求对象")
public class KnowledgeDTO {

    /** 知识库名称，不能为空 */
    @NotBlank(message = "知识库名称不能为空")
    @Schema(description = "知识库名称", example = "产品文档")
    private String name;

    /** 知识库描述 */
    @Schema(description = "知识库描述", example = "产品相关文档")
    private String description;

    /** 知识库类型：document/qa/web */
    @NotBlank(message = "知识库类型不能为空")
    @Schema(description = "知识库类型", example = "document", allowableValues = {"document", "qa", "web"})
    private String type;
}