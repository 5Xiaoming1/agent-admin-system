package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体知识库关联实体类
 * <p>
 * 映射数据库中的 agent_knowledge 中间表，存储智能体与知识库的多对多关联关系。
 * </p>
 */
@Data
@TableName("agent_knowledge")
@Schema(description = "智能体知识库关联实体")
public class AgentKnowledge {

    /** 主键ID，数据库自增 */
    @Schema(description = "关联ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 智能体ID */
    @Schema(description = "智能体ID")
    private Long agentId;

    /** 知识库ID */
    @Schema(description = "知识库ID")
    private Long knowledgeId;
}