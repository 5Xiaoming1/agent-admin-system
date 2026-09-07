package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能体实体类
 * <p>
 * 映射数据库中的 agent 表，存储 AI Agent 实例的配置信息，
 * 包括智能体名称、描述、类型、关联配置和 Token 用量等字段。
 * 使用 MyBatis-Plus 注解完成表名映射、主键策略和逻辑删除配置。
 * </p>
 *
 * <p><b>关联关系：</b></p>
 * <ul>
 *   <li>{@code setupId} → setup 表（一对一，每个 Agent 对应一个配置）</li>
 *   <li>知识库关联 → 通过 agent_knowledge 中间表（多对多）</li>
 * </ul>
 */
@Data
@TableName("agent")
@Schema(description = "智能体实体")
public class Agent {

    /** 主键ID，数据库自增 */
    @Schema(description = "智能体ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 智能体名称 */
    @Schema(description = "智能体名称")
    private String name;

    /** 智能体描述 */
    @Schema(description = "智能体描述")
    private String description;

    /** 智能体类型 */
    @Schema(description = "智能体类型")
    private String type;

    /** 关联配置ID，指向 setup 表 */
    @Schema(description = "关联配置ID")
    private Long setupId;

    /** Token用量 */
    @Schema(description = "Token用量")
    private Long tokens;

    /** 状态：1-正常，0-禁用 */
    @Schema(description = "状态：1-正常，0-禁用")
    private Integer status;

    /** 逻辑删除：0-未删除，1-已删除 */
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}