package com.tianji.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.agent.model.entity.AgentKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能体知识库关联持久层 Mapper
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper 接口，自动获得 agent_knowledge 表的 CRUD 能力。
 * </p>
 */
@Mapper
public interface AgentKnowledgeMapper extends BaseMapper<AgentKnowledge> {
}