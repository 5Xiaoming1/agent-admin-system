import { post, get, del } from "@/utils/request";
import { AI } from "./paths";
import type { ChatMessage, ChatSession } from "@/types";
import type { PaginatedResponse } from "@/types";

/**
 * AI助手API模块
 */

/**
 * 流式对话（GET方式，SSE格式）
 * @param memoryId 记忆ID
 * @param message 用户消息
 * @returns SSE流式响应对象
 */
export async function sendAiMessageStreamGet(
  memoryId: string,
  message: string,
) {
  const token = localStorage.getItem("token");
  const params = new URLSearchParams({ memoryId, message });
  const response = await fetch(`/api${AI.CHAT_GET}?${params.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`);
  }
  return response.body;
}

/**
 * 发送消息到AI助手（普通响应）
 * @param message 用户消息
 * @param sessionId 会话ID（可选）
 * @returns AI回复
 */
export function sendAiMessage(message: string, sessionId?: string) {
  return post<{ content: string; sessionId: string }>(AI.CHAT, {
    message,
    sessionId,
  });
}

/**
 * 发送消息到AI助手（流式响应）
 * @param message 用户消息
 * @param sessionId 会话ID（可选）
 * @returns SSE流式响应对象
 */
export async function sendAiMessageStream(message: string, sessionId?: string) {
  const token = localStorage.getItem("token");
  const response = await fetch(`/api${AI.CHAT_STREAM}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ message, sessionId }),
  });
  if (!response.ok) {
    throw new Error(`请求失败: ${response.status}`);
  }
  return response.body;
}

/**
 * 查询对话历史
 * @param params 分页参数
 * @returns 分页的对话历史记录
 */
export function getAiHistory(params: { page: number; pageSize: number }) {
  return get<PaginatedResponse<ChatSession>>(AI.HISTORY, params);
}

/**
 * 删除会话（统一接口，传递 ID 集合，单个/批量均调用此接口）
 * @param ids 会话记录 ID 集合
 * @returns 操作结果
 */
export function deleteAiSessions(ids: string[]) {
  return post(AI.DELETE, { ids: ids.map(Number) });
}

/**
 * 清除会话记忆
 * @returns 操作结果
 */
export function clearAiMemory() {
  return del(AI.CLEAR_MEMORY);
}
