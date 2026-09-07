package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话历史实体类（会话级存储）
 * <p>
 * 映射 chat_history 表，每条记录代表一次完整的会话，包含该会话中所有轮次的对话消息。
 * 消息内容以 JSON 数组格式存储在 messages 字段中，一条记录即一次会话。
 * </p>
 *
 * <p><b>存储模式：</b></p>
 * <ul>
 *   <li>一次会话（多轮对话）→ 一条数据库记录</li>
 *   <li>messages 字段存储完整的对话 JSON 数组</li>
 *   <li>每条消息包含 role（user/assistant）、content（内容）、time（时间）</li>
 * </ul>
 *
 * <p><b>messages JSON 格式示例：</b></p>
 * <pre>{@code
 * [
 *   {"role": "user", "content": "你好", "time": "2024-01-01T10:00:00"},
 *   {"role": "assistant", "content": "你好！有什么可以帮助你的？", "time": "2024-01-01T10:00:02"},
 *   {"role": "user", "content": "今天天气怎么样", "time": "2024-01-01T10:01:00"},
 *   {"role": "assistant", "content": "今天天气很好，适合出行。", "time": "2024-01-01T10:01:03"}
 * ]
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_history")
@Schema(description = "对话历史实体")
public class ChatHistory {

    /** 主键ID，数据库自增 */
    @Schema(description = "会话记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID，关联 user 表 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 会话唯一标识（UUID），用于区分不同会话 */
    @Schema(description = "会话唯一标识")
    private String sessionId;

    /** 会话标题，取首条用户消息 */
    @Schema(description = "会话标题")
    private String title;

    /** 会话消息列表，JSON 数组格式，存储该会话中所有轮次的对话 */
    @Schema(description = "会话消息列表（JSON数组）")
    private String messages;

    /** 消息总数（user + assistant 的总条数） */
    @Schema(description = "消息总数")
    private Integer messageCount;

    /** 逻辑删除：0-未删除，1-已删除 */
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    /** 会话创建时间 */
    @Schema(description = "会话创建时间")
    private LocalDateTime createTime;

    /** 会话最后更新时间（最后一次对话的时间） */
    @Schema(description = "会话最后更新时间")
    private LocalDateTime updateTime;
}