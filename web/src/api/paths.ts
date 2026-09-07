/**
 * 项目接口路径集中配置
 *
 * 所有接口路径统一在此文件中维护，项目中所有接口调用均引用此处的路径常量。
 * 后期接口路径变更时，只需修改此文件即可全局生效。
 */

// ==================== 登录管理 ====================
export const LOGIN = {
  /** 获取验证码（生产用） */
  CAPTCHA: "/login/captcha",
  /** 获取验证码（测试用，含明文） */
  CAPTCHA_TEST: "/login/captcha/test",
  /** 用户登录 */
  LOGIN: "/login",
} as const;

// ==================== Agent 管理 ====================
export const AGENT = {
  /** 获取 Agent 列表 */
  LIST: "/agents/list",
  /** 获取 Agent 详情 */
  DETAIL: (id: string) => `/agents/${id}`,
  /** 创建 Agent */
  CREATE: "/agents/create",
  /** 更新 Agent */
  UPDATE: (id: string) => `/agents/${id}`,
  /** 删除 Agent */
  DELETE: (id: string) => `/agents/delete/${id}`,
  /** 切换 Agent 启用/禁用状态 */
  TOGGLE_STATUS: (id: string) => `/agents/${id}/status`,
  /** 获取全部已启用 Agent */
  ALL: "/agents/all",
} as const;

// ==================== 配置管理 ====================
export const CONFIG = {
  /** 获取配置列表 */
  LIST: "/setup/list",
  /** 按名称搜索配置 */
  BY_NAME: (name: string) => `/setup/name/${encodeURIComponent(name)}`,
  /** 按 ID 获取配置 */
  BY_ID: (id: string) => `/setup/${id}`,
  /** 创建配置 */
  CREATE: "/setup/create",
  /** 更新配置 */
  UPDATE: (id: string) => `/setup/${id}`,
  /** 删除配置 */
  DELETE: (id: string) => `/setup/delete/${id}`,
  /** 切换配置启用/禁用状态 */
  TOGGLE_STATUS: (id: string) => `/setup/${id}/status`,
  /** 获取全部配置 */
  ALL: "/setup/all",
  /** 获取配置选项列表 */
  OPTIONS: "/setup/options",
} as const;

// ==================== 数据看板 ====================
export const DASHBOARD = {
  /** 获取仪表盘概览数据 */
  OVERVIEW: "/data/overview",
  /** 获取 Token 用量趋势 */
  TOKEN_TREND: "/data/token-trend",
  /** 获取 Agent 用量排行 */
  AGENT_USAGE: "/data/agent-usage",
  /** 获取 Agent 分布数据 */
  AGENT_DISTRIBUTION: "/data/agent-distribution",
} as const;

// ==================== 知识库管理 ====================
export const KNOWLEDGE = {
  /** 获取知识库列表 */
  LIST: "/knowledgebase/list",
  /** 获取知识库详情 */
  DETAIL: (id: string) => `/knowledgebase/${id}`,
  /** 创建知识库 */
  CREATE: "/knowledgebase/create",
  /** 更新知识库 */
  UPDATE: (id: string) => `/knowledgebase/${id}`,
  /** 删除知识库 */
  DELETE: (id: string) => `/knowledgebase/delete/${id}`,
  /** 获取全部已启用知识库 */
  ALL: "/knowledgebase/all",
  /** 获取知识库文件列表 */
  FILES: (id: string) => `/knowledgebase/${id}/files`,
  /** 上传文件到知识库 */
  UPLOAD_FILE: (id: string) => `/knowledgebase/${id}/files/upload`,
  /** 删除知识库文件 */
  DELETE_FILE: (id: string, fileName: string) =>
    `/knowledgebase/${id}/files/${fileName}`,
  /** 下载知识库文件 */
  DOWNLOAD_FILE: (id: string, fileName: string) =>
    `/knowledgebase/${id}/files/${fileName}/download`,
} as const;

// ==================== 工具管理 ====================
export const TOOL = {
  /** 获取工具列表 */
  LIST: "/tools/list",
  /** 获取工具详情 */
  DETAIL: (id: string) => `/tools/${id}`,
  /** 创建工具 */
  CREATE: "/tools/create",
  /** 更新工具 */
  UPDATE: (id: string) => `/tools/${id}`,
  /** 删除工具 */
  DELETE: (id: string) => `/tools/delete/${id}`,
  /** 切换工具启用/禁用状态 */
  TOGGLE_STATUS: (id: string) => `/tools/${id}/status`,
  /** 获取全部已启用工具 */
  ALL: "/tools/all",
} as const;

// ==================== 系统管理 ====================
export const SYSTEM = {
  /** 用户管理 — 获取用户列表 */
  USER_LIST: "/system/user/list",
  /** 用户管理 — 获取用户详情 */
  USER_DETAIL: (id: number) => `/system/user/${id}`,
  /** 用户管理 — 获取当前用户信息 */
  USER_PROFILE: "/system/user/profile",
  /** 用户管理 — 修改当前用户密码 */
  USER_CHANGE_PWD: "/system/user/change-pwd",
  /** 用户管理 — 创建用户 */
  USER_CREATE: "/system/user/create",
  /** 用户管理 — 更新用户 */
  USER_UPDATE: (id: number) => `/system/user/${id}`,
  /** 用户管理 — 删除用户 */
  USER_DELETE: (id: number) => `/system/user/delete/${id}`,
  /** 用户管理 — 切换用户状态 */
  USER_TOGGLE_STATUS: (id: number) => `/system/user/${id}/status`,
  /** 用户管理 — 上传头像 */
  USER_UPLOAD_AVATAR: "/system/user/upload/avatar",
  /** 登录日志 — 获取登录日志列表 */
  LOGIN_LOG_LIST: "/system/login-log/list",
  /** 登录日志 — 删除登录日志（支持单个或多个） */
  LOGIN_LOG_DELETE: "/system/login-log/delete",
  /** 登录日志 — 删除全部登录日志 */
  LOGIN_LOG_DELETE_ALL: "/system/login-log/delete/all",
  /** 操作日志 — 获取操作日志列表 */
  OPERATION_LOG_LIST: "/system/operation-log/list",
  /** 操作日志 — 删除操作日志（支持单个或多个） */
  OPERATION_LOG_DELETE: "/system/operation-log/delete",
  /** 操作日志 — 删除全部操作日志 */
  OPERATION_LOG_DELETE_ALL: "/system/operation-log/delete/all",
} as const;

// ==================== AI助手 ====================
export const AI = {
  /** 流式对话（GET方式） */
  CHAT_GET: "/ai/chat",
  /** 发送消息（普通响应） */
  CHAT: "/ai/chat",
  /** 发送消息（流式响应） */
  CHAT_STREAM: "/ai/chat/stream",
  /** 查询对话历史 */
  HISTORY: "/ai/history",
  /** 删除会话（统一传递 ID 集合，单个/批量均调用此接口） */
  DELETE: "/ai/history/delete",
  /** 清除会话记忆 */
  CLEAR_MEMORY: "/ai/memory/clear",
} as const;
