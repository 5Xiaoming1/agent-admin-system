package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.Tools;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工具管理响应 VO
 * <p>
 * 封装工具管理接口的响应数据，将数据库中的状态值（1/0）转换为前端可识别的
 * 字符串格式（active/inactive），日期字段使用 ISO 8601 格式序列化。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工具信息视图对象")
public class ToolsVO {

    /** 工具 ID */
    @JsonProperty("id")
    @Schema(description = "工具ID")
    private String id;

    /** 工具名称 */
    @Schema(description = "工具名称")
    private String name;

    /** 工具描述 */
    @Schema(description = "工具描述")
    private String description;

    /** 工具类型：api/function/builtin */
    @Schema(description = "工具类型：api-接口，function-函数，builtin-内置")
    private String type;

    /** 工具代码 */
    @Schema(description = "工具代码")
    private String code;

    /** 参数说明（JSON 对象格式：{"参数名": "参数说明"}） */
    @Schema(description = "参数说明", example = "{\"city\": \"城市名称\"}")
    private Map<String, String> parameterDescriptions;

    /** 接口地址 */
    @Schema(description = "接口地址")
    private String endpoint;

    /** 参数配置（JSON 对象格式：{"参数名": "参数类型"}） */
    @Schema(description = "参数配置", example = "{\"city\": \"string\"}")
    private Map<String, String> parameters;

    /** 状态：active（启用）/ inactive（禁用） */
    @Schema(description = "状态：active-启用，inactive-禁用")
    private String status;

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
     * 将数据库实体转换为 VO（不含代码内容）
     * <p>
     * 将数据库中的状态值 1/0 转换为前端可识别的 active/inactive 字符串，
     * 并将 Long 类型的 ID 转为 String 类型。
     * 代码内容需要从文件中读取，调用方需自行通过 {@link #setCode(String)} 设置。
     * </p>
     *
     * @param entity 数据库实体对象
     * @return 转换后的 VO 对象（code 字段为空）
     */
    public static ToolsVO fromEntity(Tools entity) {
        return ToolsVO.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .endpoint(entity.getEndpoint())
                .parameters(parseParameters(entity.getParameters()))
                .status(entity.getStatus() == 1 ? "active" : "inactive")
                .createdAt(entity.getCreateTime())
                .updatedAt(entity.getUpdateTime())
                .build();
    }

    private static Map<String, String> parseParameters(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}