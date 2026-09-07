package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置选项 VO（下拉选择专用）
 * <p>
 * 仅包含 id 和 name 两个字段，用于 Agent 创建/编辑时下拉选择配置。
 * 不包含 apiKey 等敏感信息，也不涉及 AES 解密，更轻量安全。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "配置选项视图对象（下拉选择专用）")
public class SetupOptionVO {

    /** 配置 ID */
    @JsonProperty("id")
    @Schema(description = "配置ID")
    private String id;

    /** 配置名称 */
    @Schema(description = "配置名称")
    private String name;

    /**
     * 将数据库实体转换为选项 VO
     * <p>
     * 仅提取 id 和 name，不处理 apiKey 等敏感字段。
     * </p>
     *
     * @param entity 数据库实体对象
     * @return 转换后的选项 VO 对象
     */
    public static SetupOptionVO fromEntity(com.tianji.agent.model.entity.Setup entity) {
        return SetupOptionVO.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .build();
    }
}