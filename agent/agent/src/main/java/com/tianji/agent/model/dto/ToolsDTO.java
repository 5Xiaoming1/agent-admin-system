package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 工具管理请求 DTO
 * <p>
 * 用于新增和修改工具时接收前端提交的请求参数，
 * 包含工具名称、描述、类型、代码和参数说明。
 * </p>
 */
@Data
@Schema(description = "工具管理请求对象")
public class ToolsDTO {

    /** 工具名称，不能为空 */
    @NotBlank(message = "工具名称不能为空")
    @Schema(description = "工具名称", example = "天气查询")
    private String name;

    /** 工具描述 */
    @Schema(description = "工具描述", example = "查询天气信息")
    private String description;

    /** 工具类型：api/function/builtin */
    @NotBlank(message = "工具类型不能为空")
    @Schema(description = "工具类型", example = "api", allowableValues = {"api", "function", "builtin"})
    private String type;

    /** 工具代码 */
    @Schema(description = "工具代码", example = "async function queryWeather(city) { ... }")
    private String code;

    /** 参数说明（JSON 对象格式：{"参数名": "参数说明"}） */
    @Schema(description = "参数说明", example = "{\"city\": \"城市名称\"}")
    private Map<String, String> parameterDescriptions;

    /** 接口地址 */
    @Schema(description = "接口地址", example = "https://api.weather.com/v1")
    private String endpoint;

    /** 参数配置（JSON 对象格式：{"参数名": "参数类型"}） */
    @Schema(description = "参数配置", example = "{\"city\": \"string\"}")
    private Map<String, String> parameters;
}