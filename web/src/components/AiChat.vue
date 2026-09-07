<template>
  <div class="ai-chat">
    <el-button
      class="ai-assistant-btn"
      type="primary"
      :icon="ChatDotRound"
      @click="openChat"
    >
      AI助手
    </el-button>

    <div
      v-show="dialogVisible"
      class="chat-window"
      :style="windowStyle"
      @mousedown="bringToFront"
    >
      <div class="chat-header" @mousedown="startDrag">
        <span class="chat-title">AI助手</span>
        <div class="header-actions">
          <el-tooltip content="新会话" placement="bottom">
            <el-icon class="action-btn" @click.stop="handleNewSession">
              <Plus />
            </el-icon>
          </el-tooltip>
          <el-tooltip content="会话历史" placement="bottom">
            <el-icon class="action-btn" @click.stop="openHistoryDialog">
              <Clock />
            </el-icon>
          </el-tooltip>
          <el-icon class="action-btn" @click.stop="closeChat">
            <Close />
          </el-icon>
        </div>
      </div>

      <div class="chat-body">
        <div class="chat-messages" ref="messagesRef">
          <div v-if="historyLoading" class="history-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            加载历史记录...
          </div>
          <div v-else-if="hasMore" class="load-more" @click="loadMoreHistory">
            <el-icon><Top /></el-icon>
            加载更多历史记录
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message"
            :class="msg.role"
          >
            <div
              v-if="msg.role === 'assistant'"
              class="message-content markdown-body"
              v-html="renderMarkdown(msg.content)"
            ></div>
            <div v-else class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ msg.createTime }}</div>
          </div>

          <div v-if="thinking" class="message assistant">
            <div class="message-content thinking">
              <el-icon class="is-loading"><Loading /></el-icon>
              思考中...
            </div>
          </div>

          <div v-else-if="streaming" class="message assistant">
            <div
              class="message-content streaming markdown-body"
              v-html="streamingHtml"
            ></div>
          </div>

          <div v-else-if="loading" class="message assistant">
            <div class="message-content loading">
              <span class="loading-dots">
                <span>.</span><span>.</span><span>.</span>
              </span>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="请输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="loading || streaming"
          >
            <template #append>
              <el-button
                type="primary"
                :icon="Promotion"
                :disabled="loading || streaming || !inputMessage.trim()"
                @click="sendMessage"
              />
            </template>
          </el-input>
        </div>
      </div>

      <div
        class="resize-handle resize-handle-right"
        @mousedown="startResize('right')"
      ></div>
      <div
        class="resize-handle resize-handle-bottom"
        @mousedown="startResize('bottom')"
      ></div>
      <div
        class="resize-handle resize-handle-corner"
        @mousedown="startResize('corner')"
      ></div>
      <div
        class="resize-handle resize-handle-left"
        @mousedown="startResize('left')"
      ></div>
      <div
        class="resize-handle resize-handle-top"
        @mousedown="startResize('top')"
      ></div>
      <div
        class="resize-handle resize-handle-top-left"
        @mousedown="startResize('top-left')"
      ></div>
      <div
        class="resize-handle resize-handle-top-right"
        @mousedown="startResize('top-right')"
      ></div>
      <div
        class="resize-handle resize-handle-bottom-left"
        @mousedown="startResize('bottom-left')"
      ></div>
    </div>

    <!-- 会话历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="会话历史"
      width="600px"
      :z-index="zIndex + 1"
      @opened="loadSessionHistory"
    >
      <div v-loading="sessionHistoryLoading" class="session-history-list">
        <div
          v-for="session in sessionHistoryList"
          :key="session.id"
          class="session-item"
          :class="{ selected: selectedSessions.has(session.id) }"
        >
          <el-checkbox
            :model-value="selectedSessions.has(session.id)"
            @change="toggleSelect(session.id)"
            @click.stop
          />
          <div class="session-item-content" @click="loadSession(session)">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-info">
              <span class="session-time">{{
                formatTime(session.updateTime)
              }}</span>
              <span class="session-count"
                >{{ Math.ceil(session.messageCount / 2) }} 次对话</span
              >
            </div>
          </div>
          <el-icon
            class="session-delete-btn"
            @click.stop="deleteSingleSession(session)"
          >
            <Delete />
          </el-icon>
        </div>
        <el-empty
          v-if="!sessionHistoryLoading && sessionHistoryList.length === 0"
          description="暂无会话历史"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <div class="footer-left">
            <el-button
              type="danger"
              plain
              :disabled="selectedSessions.size === 0"
              @click="batchDeleteSessions"
            >
              批量删除 ({{ selectedSessions.size }})
            </el-button>
            <el-button
              type="danger"
              :disabled="sessionHistoryList.length === 0"
              @click="deleteAllSessions"
            >
              全部删除
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, reactive, computed } from "vue";
import {
  ChatDotRound,
  Promotion,
  Loading,
  Close,
  Plus,
  Top,
  Clock,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { marked } from "marked";
import {
  sendAiMessage,
  sendAiMessageStream,
  getAiHistory,
  deleteAiSessions,
} from "@/api/ai";
import type { ChatSession, ChatMessage } from "@/types";

marked.setOptions({ breaks: true, gfm: true });

function renderMarkdown(text: string): string {
  return marked.parse(text) as string;
}

const dialogVisible = ref(false);
const inputMessage = ref("");
const loading = ref(false);
const thinking = ref(false);
const streaming = ref(false);
const streamingContent = ref("");
const streamingHtml = computed(
  () =>
    renderMarkdown(streamingContent.value) + '<span class="cursor">|</span>',
);
const historyLoading = ref(false);
const messagesRef = ref<HTMLElement>();
const zIndex = ref(1000);

const HISTORY_PAGE_SIZE = 20;
const historyPage = ref(1);
const historyTotal = ref(0);
const hasMore = computed(() => messages.value.length < historyTotal.value);

// 会话历史对话框相关
const historyDialogVisible = ref(false);
const sessionHistoryLoading = ref(false);
const sessionHistoryList = ref<ChatSession[]>([]);
const selectedSessions = ref<Set<string>>(new Set());

// 当前会话ID
const currentSessionId = ref<string | undefined>(undefined);

const windowState = reactive({
  x: window.innerWidth - 540,
  y: 80,
  width: 500,
  height: 650,
  minWidth: 350,
  minHeight: 300,
});

const windowStyle = computed(() => ({
  left: `${windowState.x}px`,
  top: `${windowState.y}px`,
  width: `${windowState.width}px`,
  height: `${windowState.height}px`,
  zIndex: zIndex.value,
}));

interface Message {
  role: "user" | "assistant";
  content: string;
  createTime: string;
}

const messages = ref<Message[]>([]);

function formatTime(dateStr: string) {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${month}-${day} ${hours}:${minutes}`;
}

function parseMessagesField(raw: unknown): ChatMessage[] | null {
  if (!raw) return null;
  if (Array.isArray(raw)) return raw as ChatMessage[];
  if (typeof raw === "string") {
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : null;
    } catch {
      return null;
    }
  }
  return null;
}

function openChat() {
  dialogVisible.value = true;
  bringToFront();
  // 每次打开默认创建新对话
  messages.value = [];
  currentSessionId.value = crypto.randomUUID();
}

function closeChat() {
  dialogVisible.value = false;
}

function bringToFront() {
  zIndex.value++;
}

async function loadHistory() {
  historyLoading.value = true;
  try {
    const res = await getAiHistory({ page: 1, pageSize: HISTORY_PAGE_SIZE });
    const list = res.list || [];
    historyTotal.value = res.total || 0;

    // 解析会话列表，提取所有消息
    const allMessages: Message[] = [];
    for (const session of list) {
      const msgs = parseMessagesField(session.messages);
      if (msgs) {
        for (const msg of msgs) {
          allMessages.push({
            role: msg.role,
            content: msg.content,
            createTime: formatTime(msg.time || session.createTime),
          });
        }
      }
    }

    messages.value = allMessages.reverse();
    historyPage.value = 1;
  } catch {
    ElMessage.error("加载历史记录失败");
  } finally {
    historyLoading.value = false;
  }
}

async function loadMoreHistory() {
  const nextPage = historyPage.value + 1;
  historyLoading.value = true;
  try {
    const res = await getAiHistory({
      page: nextPage,
      pageSize: HISTORY_PAGE_SIZE,
    });
    const list = res.list || [];
    historyTotal.value = res.total || 0;

    // 解析新加载的会话消息
    const newMessages: Message[] = [];
    for (const session of list) {
      const msgs = parseMessagesField(session.messages);
      if (msgs) {
        for (const msg of msgs) {
          newMessages.push({
            role: msg.role,
            content: msg.content,
            createTime: formatTime(msg.time || session.createTime),
          });
        }
      }
    }

    messages.value = [...newMessages.reverse(), ...messages.value];
    historyPage.value = nextPage;
  } catch {
    ElMessage.error("加载历史记录失败");
  } finally {
    historyLoading.value = false;
  }
}

function handleNewSession() {
  messages.value = [];
  // 生成新的sessionId，确保后端创建全新会话
  currentSessionId.value = crypto.randomUUID();
  historyTotal.value = 0;
  historyPage.value = 1;
  ElMessage.success("已开启新会话");
}

function openHistoryDialog() {
  historyDialogVisible.value = true;
}

async function loadSessionHistory() {
  sessionHistoryLoading.value = true;
  try {
    const res = await getAiHistory({ page: 1, pageSize: 50 });
    sessionHistoryList.value = res.list || [];
  } catch {
    ElMessage.error("加载会话历史失败");
  } finally {
    sessionHistoryLoading.value = false;
  }
}

function loadSession(session: ChatSession) {
  currentSessionId.value = session.sessionId;
  const msgs = parseMessagesField(session.messages);
  if (msgs) {
    messages.value = msgs.map((msg) => ({
      role: msg.role,
      content: msg.content,
      createTime: formatTime(msg.time || session.createTime),
    }));
  }
  historyDialogVisible.value = false;
}

function toggleSelect(id: string) {
  if (selectedSessions.value.has(id)) {
    selectedSessions.value.delete(id);
  } else {
    selectedSessions.value.add(id);
  }
}

async function deleteSingleSession(session: ChatSession) {
  try {
    await ElMessageBox.confirm(
      `确定删除会话"${session.title}"吗？`,
      "确认删除",
      { type: "warning" },
    );
    await deleteAiSessions([session.id]);
    ElMessage.success("删除成功");
    await loadSessionHistory();
    selectedSessions.value.clear();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error("删除失败");
    }
  }
}

async function batchDeleteSessions() {
  if (selectedSessions.value.size === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${selectedSessions.value.size} 个会话吗？`,
      "确认删除",
      { type: "warning" },
    );
    await deleteAiSessions([...selectedSessions.value]);
    ElMessage.success("批量删除成功");
    await loadSessionHistory();
    selectedSessions.value.clear();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error("批量删除失败");
    }
  }
}

async function deleteAllSessions() {
  if (sessionHistoryList.value.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除全部 ${sessionHistoryList.value.length} 个会话吗？此操作不可恢复。`,
      "确认删除",
      { type: "warning" },
    );
    const allIds = sessionHistoryList.value.map((s) => s.id);
    await deleteAiSessions(allIds);
    ElMessage.success("全部删除成功");
    await loadSessionHistory();
    selectedSessions.value.clear();
  } catch (e) {
    if (e !== "cancel") {
      ElMessage.error("全部删除失败");
    }
  }
}

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

function startDrag(e: MouseEvent) {
  isDragging = true;
  dragOffsetX = e.clientX - windowState.x;
  dragOffsetY = e.clientY - windowState.y;
  bringToFront();

  document.addEventListener("mousemove", onDrag);
  document.addEventListener("mouseup", stopDrag);
  e.preventDefault();
}

function onDrag(e: MouseEvent) {
  if (!isDragging) return;
  windowState.x = e.clientX - dragOffsetX;
  windowState.y = e.clientY - dragOffsetY;

  const maxX = window.innerWidth - windowState.width;
  const maxY = window.innerHeight - 40;
  windowState.x = Math.max(0, Math.min(windowState.x, maxX));
  windowState.y = Math.max(0, Math.min(windowState.y, maxY));
}

function stopDrag() {
  isDragging = false;
  document.removeEventListener("mousemove", onDrag);
  document.removeEventListener("mouseup", stopDrag);
}

let isResizing = false;
let resizeType = "";
let startX = 0;
let startY = 0;
let startWidth = 0;
let startHeight = 0;
let startLeft = 0;
let startTop = 0;

function startResize(type: string) {
  isResizing = true;
  resizeType = type;
  startX = event.clientX;
  startY = event.clientY;
  startWidth = windowState.width;
  startHeight = windowState.height;
  startLeft = windowState.x;
  startTop = windowState.y;
  bringToFront();

  document.addEventListener("mousemove", onResize);
  document.addEventListener("mouseup", stopResize);
  event.preventDefault();
}

function onResize(e: MouseEvent) {
  if (!isResizing) return;

  const deltaX = e.clientX - startX;
  const deltaY = e.clientY - startY;

  if (
    resizeType === "right" ||
    resizeType === "corner" ||
    resizeType === "top-right"
  ) {
    windowState.width = Math.max(windowState.minWidth, startWidth + deltaX);
  }

  if (
    resizeType === "bottom" ||
    resizeType === "corner" ||
    resizeType === "bottom-left"
  ) {
    windowState.height = Math.max(windowState.minHeight, startHeight + deltaY);
  }

  if (resizeType === "left" || resizeType === "top-left") {
    const newWidth = Math.max(windowState.minWidth, startWidth - deltaX);
    windowState.width = newWidth;
    windowState.x = startLeft + (startWidth - newWidth);
  }

  if (
    resizeType === "top" ||
    resizeType === "top-left" ||
    resizeType === "top-right"
  ) {
    const newHeight = Math.max(windowState.minHeight, startHeight - deltaY);
    windowState.height = newHeight;
    windowState.y = startTop + (startHeight - newHeight);
  }
}

function stopResize() {
  isResizing = false;
  resizeType = "";
  document.removeEventListener("mousemove", onResize);
  document.removeEventListener("mouseup", stopResize);
}

async function sendMessage() {
  if (!inputMessage.value.trim() || loading.value || streaming.value) return;

  const userMessage = inputMessage.value.trim();
  const now = formatTime(new Date().toISOString());
  messages.value.push({ role: "user", content: userMessage, createTime: now });
  inputMessage.value = "";
  thinking.value = true;
  streamingContent.value = "";

  await scrollToBottom();

  try {
    const body = await sendAiMessageStream(userMessage, currentSessionId.value);
    if (!body) {
      throw new Error("无法获取流式响应");
    }

    thinking.value = false;
    streaming.value = true;

    const reader = body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");
      buffer = lines.pop() || "";

      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed || !trimmed.startsWith("data:")) continue;

        const data = trimmed.slice(5).trimStart();
        if (data === "[DONE]") continue;

        try {
          const parsed = JSON.parse(data);
          if (parsed.content) {
            streamingContent.value += parsed.content;
          } else if (parsed.sessionId) {
            currentSessionId.value = parsed.sessionId;
          } else {
            streamingContent.value += data;
          }
        } catch {
          // 忽略解析失败的行
        }
      }
      await scrollToBottom();
    }

    if (streamingContent.value) {
      messages.value.push({
        role: "assistant",
        content: streamingContent.value,
        createTime: formatTime(new Date().toISOString()),
      });
    }
  } catch {
    // 流式请求失败，回退到普通请求
    try {
      loading.value = true;
      const response = await sendAiMessage(userMessage, currentSessionId.value);
      let content = "";

      if (typeof response === "object" && response !== null) {
        content = response.content || JSON.stringify(response);
        if (response.sessionId) {
          currentSessionId.value = response.sessionId;
        }
      } else if (typeof response === "string") {
        try {
          const parsed = JSON.parse(response);
          content = parsed.content || response;
          if (parsed.sessionId) {
            currentSessionId.value = parsed.sessionId;
          }
        } catch {
          content = response;
        }
      } else {
        content = String(response || "");
      }

      messages.value.push({
        role: "assistant",
        content: content,
        createTime: formatTime(new Date().toISOString()),
      });
    } catch {
      ElMessage.error("发送消息失败，请稍后重试");
    } finally {
      loading.value = false;
    }
  } finally {
    thinking.value = false;
    streaming.value = false;
    streamingContent.value = "";
    loading.value = false;
    await scrollToBottom();
  }
}

async function scrollToBottom() {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
}
</script>

<style scoped lang="scss">
.ai-assistant-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chat-window {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  cursor: move;
  user-select: none;

  .chat-title {
    font-size: 16px;
    font-weight: 600;
  }

  .header-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      cursor: pointer;
      font-size: 18px;
      padding: 4px;
      border-radius: 4px;
      transition: background 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

.chat-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .history-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px;
    color: #909399;
    font-size: 13px;
  }

  .load-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px;
    color: #409eff;
    font-size: 13px;
    cursor: pointer;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: #ecf5ff;
    }
  }

  .message {
    display: flex;
    flex-direction: column;

    &.user {
      align-items: flex-end;

      .message-content {
        background: #409eff;
        color: #fff;
        border-radius: 12px 12px 0 12px;
      }

      .message-time {
        text-align: right;
      }
    }

    &.assistant {
      align-items: flex-start;

      .message-content {
        background: #f0f2f5;
        color: #303133;
        border-radius: 12px 12px 12px 0;
      }

      .message-time {
        text-align: left;
      }
    }

    .message-content {
      max-width: 80%;
      padding: 10px 16px;
      word-break: break-word;

      &.loading {
        display: flex;
        align-items: center;
        gap: 8px;

        .loading-dots {
          display: inline-flex;
          gap: 2px;

          span {
            animation: dotPulse 1.4s infinite;
            font-size: 24px;
            font-weight: bold;
            color: #909399;

            &:nth-child(1) {
              animation-delay: 0s;
            }

            &:nth-child(2) {
              animation-delay: 0.2s;
            }

            &:nth-child(3) {
              animation-delay: 0.4s;
            }
          }
        }
      }

      &.thinking {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #909399;
        font-size: 14px;
      }

      &.streaming {
        .cursor {
          animation: blink 1s step-end infinite;
        }
      }
    }

    .message-time {
      font-size: 11px;
      color: #c0c4cc;
      margin-top: 4px;
      padding: 0 4px;
    }
  }
}

.markdown-body {
  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 12px 0 8px;
    font-weight: 600;
    line-height: 1.4;

    &:first-child {
      margin-top: 0;
    }
  }

  :deep(h1) {
    font-size: 1.4em;
  }
  :deep(h2) {
    font-size: 1.25em;
  }
  :deep(h3) {
    font-size: 1.1em;
  }

  :deep(p) {
    margin: 6px 0;
    line-height: 1.6;

    &:first-child {
      margin-top: 0;
    }
    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    margin: 6px 0;
    padding-left: 24px;
    line-height: 1.6;
  }

  :deep(li) {
    margin: 2px 0;
  }

  :deep(code) {
    background: rgba(0, 0, 0, 0.06);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.9em;
    font-family: Consolas, Monaco, "Andale Mono", monospace;
  }

  :deep(pre) {
    background: #282c34;
    color: #abb2bf;
    padding: 12px 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 8px 0;
    line-height: 1.5;

    code {
      background: none;
      padding: 0;
      color: inherit;
      font-size: 0.9em;
    }
  }

  :deep(blockquote) {
    border-left: 3px solid #409eff;
    padding: 8px 12px;
    margin: 8px 0;
    background: rgba(64, 158, 255, 0.05);
    border-radius: 0 4px 4px 0;
    color: #606266;
  }

  :deep(table) {
    border-collapse: collapse;
    width: 100%;
    margin: 8px 0;
    font-size: 0.9em;

    th,
    td {
      border: 1px solid #e6e6e6;
      padding: 6px 10px;
      text-align: left;
    }

    th {
      background: #f5f7fa;
      font-weight: 600;
    }

    tr:nth-child(even) {
      background: #fafafa;
    }
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid #e6e6e6;
    margin: 12px 0;
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(strong) {
    font-weight: 600;
    color: #303133;
  }

  :deep(.cursor) {
    animation: blink 1s step-end infinite;
  }
}

.chat-input {
  border-top: 1px solid #e6e6e6;
  padding: 12px;
}

.resize-handle {
  position: absolute;
  z-index: 10;

  &.resize-handle-right {
    right: 0;
    top: 0;
    width: 6px;
    height: 100%;
    cursor: ew-resize;
  }

  &.resize-handle-bottom {
    bottom: 0;
    left: 0;
    width: 100%;
    height: 6px;
    cursor: ns-resize;
  }

  &.resize-handle-corner {
    right: 0;
    bottom: 0;
    width: 16px;
    height: 16px;
    cursor: nwse-resize;
  }

  &.resize-handle-left {
    left: 0;
    top: 0;
    width: 6px;
    height: 100%;
    cursor: ew-resize;
  }

  &.resize-handle-top {
    top: 0;
    left: 0;
    width: 100%;
    height: 6px;
    cursor: ns-resize;
  }

  &.resize-handle-top-left {
    left: 0;
    top: 0;
    width: 16px;
    height: 16px;
    cursor: nwse-resize;
  }

  &.resize-handle-top-right {
    right: 0;
    top: 0;
    width: 16px;
    height: 16px;
    cursor: nesw-resize;
  }

  &.resize-handle-bottom-left {
    left: 0;
    bottom: 0;
    width: 16px;
    height: 16px;
    cursor: nesw-resize;
  }
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

@keyframes dotPulse {
  0%,
  20% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
  80%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
}

.session-history-list {
  max-height: 400px;
  overflow-y: auto;

  .session-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border: 1px solid #e6e6e6;
    border-radius: 8px;
    margin-bottom: 8px;
    transition: all 0.2s;

    &.selected {
      background: #ecf5ff;
      border-color: #409eff;
    }

    &:hover {
      background: #f5f7fa;
      border-color: #409eff;
    }

    .session-item-content {
      flex: 1;
      min-width: 0;
      cursor: pointer;
    }

    .session-delete-btn {
      flex-shrink: 0;
      cursor: pointer;
      color: #c0c4cc;
      font-size: 18px;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: #f56c6c;
        background: #fef0f0;
      }
    }

    .session-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .session-info {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #909399;

      .session-time {
        color: #909399;
      }

      .session-count {
        color: #409eff;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;

  .footer-left {
    display: flex;
    gap: 8px;
  }
}
</style>
