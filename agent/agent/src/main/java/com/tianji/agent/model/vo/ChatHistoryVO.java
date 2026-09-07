package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.ChatHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话历史响应对象（会话级）
 * <p>
 * 每条记录代表一次完整的会话，messages 字段包含该会话中所有轮次的对话消息（JSON 数组）。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话历史（会话级）")
public class ChatHistoryVO {

    /** 会话记录 ID */
    @JsonProperty("id")
    @Schema(description = "会话记录ID")
    private String id;

    /** 用户 ID */
    @Schema(description = "用户ID")
    private Long userId;

    /** 会话唯一标识 */
    @JsonProperty("sessionId")
    @Schema(description = "会话唯一标识")
    private String sessionId;

    /** 会话标题（首条用户消息） */
    @Schema(description = "会话标题")
    private String title;

    /** 会话消息列表（JSON 数组），包含该会话中所有轮次的对话 */
    @Schema(description = "会话消息列表（JSON数组）")
    private String messages;

    /** 消息总数 */
    @JsonProperty("messageCount")
    @Schema(description = "消息总数")
    private Integer messageCount;

    /** 会话创建时间 */
    @JsonProperty("createTime")
    @Schema(description = "会话创建时间")
    private LocalDateTime createTime;

    /** 会话最后更新时间 */
    @JsonProperty("updateTime")
    @Schema(description = "会话最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 将数据库实体转换为 VO
     *
     * @param entity 数据库实体对象
     * @return 转换后的 VO 对象
     */
    public static ChatHistoryVO fromEntity(ChatHistory entity) {
        if (entity == null) {
            return null;
        }
        return ChatHistoryVO.builder()
                .id(String.valueOf(entity.getId()))
                .userId(entity.getUserId())
                .sessionId(entity.getSessionId())
                .title(entity.getTitle())
                .messages(entity.getMessages())
                .messageCount(entity.getMessageCount())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}