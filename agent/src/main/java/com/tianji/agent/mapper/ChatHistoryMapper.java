package com.tianji.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.agent.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话历史持久层 Mapper
 * <p>
 * 继承 MyBatis-Plus 的 BaseMapper 接口，自动获得 chat_history 表的 CRUD 能力。
 * </p>
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
}