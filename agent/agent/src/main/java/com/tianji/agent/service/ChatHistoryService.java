package com.tianji.agent.service;

import com.tianji.agent.model.entity.ChatHistory;
import com.tianji.agent.model.vo.ChatHistoryVO;
import com.tianji.agent.model.vo.PageVO;

import java.util.List;

/**
 * 对话历史服务接口（会话级存储）
 * <p>
 * 以会话为单位存储对话历史，每次会话（多轮对话）作为一条数据库记录。
 * 提供会话级别的新增消息追加、分页查询和清除功能。
 * </p>
 */
public interface ChatHistoryService {

    /**
     * 向会话中追加一条消息
     * <p>
     * 如果 sessionId 对应的会话不存在，则创建新会话，并自动将首条用户消息设为会话标题；
     * 如果会话已存在，则将消息追加到 messages JSON 数组末尾。
     * 如果 sessionId 为 null 或空，则自动查找该用户最新的活跃会话进行追加；
     * 如果用户没有任何活跃会话，则创建新会话。
     * </p>
     *
     * @param userId    用户 ID
     * @param sessionId 会话唯一标识（可为 null，此时自动关联最新会话）
     * @param role      消息角色（user / assistant）
     * @param content   消息内容
     */
    void saveMessage(Long userId, String sessionId, String role, String content);

    /**
     * 分页查询当前用户的会话列表
     * <p>
     * 按会话最后更新时间倒序排列，返回会话级数据（每条记录包含完整的多轮对话）。
     * </p>
     *
     * @param userId   用户 ID
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @return 分页结果，每条记录代表一次完整会话
     */
    PageVO<ChatHistoryVO> page(Long userId, int page, int pageSize);

    /**
     * 清除用户的所有会话历史
     * <p>
     * 逻辑删除该用户的所有会话记录。
     * </p>
     *
     * @param userId 用户 ID
     */
    void clearByUserId(Long userId);

    /**
     * 查找用户最新的活跃会话
     * <p>
     * 按更新时间倒序排列，返回第一条未删除的会话记录。
     * </p>
     *
     * @param userId 用户 ID
     * @return 最新活跃会话，如无则返回 null
     */
    ChatHistory findLatestSession(Long userId);

    /**
     * 删除会话历史
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。传递几个 ID 就删除几个，传 1 个则删 1 个。
     * </p>
     *
     * @param ids 会话记录 ID 集合
     */
    void deleteByIds(List<Long> ids);
}