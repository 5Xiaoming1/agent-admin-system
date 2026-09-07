package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.Agent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体管理响应 VO
 * <p>
 * 封装智能体管理接口的响应数据，将数据库中的状态值（1/0）转换为前端可识别的
 * 字符串格式（active/inactive），日期字段使用 ISO 8601 格式序列化。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能体信息视图对象")
public class AgentVO {

    /** 智能体 ID */
    @JsonProperty("id")
    @Schema(description = "智能体ID")
    private String id;

    /** 智能体名称 */
    @Schema(description = "智能体名称")
    private String name;

    /** 智能体描述 */
    @Schema(description = "智能体描述")
    private String description;

    /** 智能体类型 */
    @Schema(description = "智能体类型")
    private String type;

    /** 关联配置 ID */
    @JsonProperty("setupId")
    @Schema(description = "关联配置ID")
    private String setupId;

    /** 关联知识库 ID 列表 */
    @JsonProperty("knowledgeIds")
    @Schema(description = "关联知识库ID列表")
    private List<Long> knowledgeIds;

    /** Token 用量 */
    @Schema(description = "Token用量")
    private Long tokens;

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
     * 并将 Long 类型的 ID 和 setupId 转为 String 类型。
     * knowledgeIds 通过中间表查询后由调用方传入。
     * </p>
     *
     * @param entity       数据库实体对象
     * @param knowledgeIds 关联知识库ID列表，通过 agent_knowledge 中间表查询
     * @return 转换后的 VO 对象
     */
    public static AgentVO fromEntity(Agent entity, List<Long> knowledgeIds) {
        return AgentVO.builder()
                .id(String.valueOf(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .setupId(entity.getSetupId() != null ? String.valueOf(entity.getSetupId()) : null)
                .knowledgeIds(knowledgeIds)
                .tokens(entity.getTokens())
                .status(entity.getStatus() == 1 ? "active" : "inactive")
                .createdAt(entity.getCreateTime())
                .updatedAt(entity.getUpdateTime())
                .build();
    }
}