package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI对话请求参数
 * <p>
 * 前端发送消息到AI助手时提交的请求体，包含用户消息内容和会话标识。
 * 传入 sessionId 可继续已有会话，不传则自动创建新会话。
 * </p>
 */
@Data
@Schema(description = "AI对话请求参数")
public class ChatDTO {

    /** 用户消息内容，不能为空 */
    @NotBlank(message = "消息不能为空")
    @Schema(description = "用户消息内容", example = "你好，请介绍一下你自己")
    private String message;

    /** 会话标识（可选），传入则继续已有会话，不传则自动创建新会话 */
    @Schema(description = "会话标识（可选），传入则继续已有会话，不传则创建新会话", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;
}