package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 智能体管理请求 DTO
 * <p>
 * 用于新增和修改智能体时接收前端提交的请求参数，
 * 包含智能体名称、描述、类型、关联配置、关联知识库、关联工具和 Token 用量。
 * </p>
 */
@Data
@Schema(description = "智能体管理请求对象")
public class AgentDTO {

    /** 智能体名称，不能为空 */
    @NotBlank(message = "智能体名称不能为空")
    @Schema(description = "智能体名称", example = "智能客服助手")
    private String name;

    /** 智能体描述 */
    @Schema(description = "智能体描述", example = "负责处理用户日常咨询的智能客服")
    private String description;

    /** 智能体类型 */
    @NotBlank(message = "智能体类型不能为空")
    @Schema(description = "智能体类型", example = "chat", allowableValues = {"chat", "task", "workflow"})
    private String type;

    /** 关联配置 ID，指向 setup 表 */
    @Schema(description = "关联配置ID", example = "1")
    private Long setupId;

    /** 关联知识库 ID 列表 */
    @Schema(description = "关联知识库ID列表", example = "[1, 2, 3]")
    private List<Long> knowledgeIds;

    /** Token 用量 */
    @Schema(description = "Token用量", example = "10000")
    private Long tokens;
}