package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 配置管理请求 DTO
 * <p>
 * 用于新增和修改配置时接收前端提交的请求参数，
 * 包含配置名称、API 密钥和模型地址。
 * </p>
 */
@Data
@Schema(description = "配置管理请求对象")
public class SetupDTO {

    /** 配置名称，不能为空 */
    @NotBlank(message = "配置名称不能为空")
    @Schema(description = "配置名称", example = "OpenAI 主配置")
    private String name;

    /** API 密钥，不能为空 */
    @NotBlank(message = "API密钥不能为空")
    @Schema(description = "API密钥", example = "sk-xxxxxxxxxxxx")
    private String apiKey;

    /** 模型地址，不能为空 */
    @NotBlank(message = "模型地址不能为空")
    @Schema(description = "模型地址", example = "https://api.openai.com")
    private String baseUrl;
}