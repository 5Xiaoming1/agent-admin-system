package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 配置管理响应 VO
 * <p>
 * 封装配置管理接口的响应数据，将数据库中的状态值（1/0）转换为前端可识别的
 * 字符串格式（active/inactive），日期字段使用 ISO 8601 格式序列化。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配置信息视图对象")
public class SetupVO {

    /** 配置 ID */
    @JsonProperty("id")
    @Schema(description = "配置ID")
    private String id;

    /** 配置名称 */
    @Schema(description = "配置名称")
    private String name;

    /** API 密钥 */
    @Schema(description = "API密钥")
    private String apiKey;

    /** 模型地址 */
    @Schema(description = "模型地址")
    private String baseUrl;

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
     * 将数据库实体转换为 VO
     * <p>
     * 将数据库中的状态值 1/0 转换为前端可识别的 active/inactive 字符串，
     * 并将 Long 类型的 ID 转为 String 类型。
     * </p>
     *
     * @param entity 数据库实体对象
     * @return 转换后的 VO 对象
     */
    public static SetupVO fromEntity(com.tianji.agent.model.entity.Setup entity) {
        return SetupVO.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .apiKey(entity.getApiKey())
                .baseUrl(entity.getBaseUrl())
                .status(entity.getStatus() == 1 ? "active" : "inactive")
                .createdAt(entity.getCreateTime())
                .updatedAt(entity.getUpdateTime())
                .build();
    }
}