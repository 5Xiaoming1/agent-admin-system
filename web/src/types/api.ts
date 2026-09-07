export interface LoginRequest {
  username: string;
  password: string;
  captcha: string;
  captchaKey: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  avatar?: string;
  admin: number;
}

export interface CaptchaResponse {
  captchaImage: string;
  captchaKey: string;
  captchaCode?: string | null;
}

export interface ApiConfig {
  id: string;
  name: string;
  apiKey: string;
  baseUrl: string;
  status: "active" | "inactive";
  createdAt: string;
  updatedAt: string;
}

export interface Agent {
  id: string;
  name: string;
  description: string;
  type: "chat" | "task" | "workflow";
  setupId: string;
  knowledgeIds?: number[];
  status: "active" | "inactive";
  tokens: number;
  createdAt: string;
  updatedAt: string;
}

export interface KnowledgeFile {
  name: string;
  size: number;
  mimeType: string;
  lastModified: string;
}

export interface KnowledgeBase {
  id: string;
  name: string;
  description: string;
  type: "document" | "qa" | "web";
  documentCount: number;
  status: "active" | "inactive";
  files?: KnowledgeFile[];
  createdAt: string;
  updatedAt: string;
}

export interface Tool {
  id: string;
  name: string;
  description: string;
  type: "api" | "function" | "builtin";
  code: string;
  parameterDescriptions: Record<string, string>;
  parameters?: Record<string, any>;
  endpoint?: string;
  status: "active" | "inactive";
  createdAt: string;
  updatedAt: string;
}

export interface DashboardData {
  totalTokens: number;
  activeAgentCount: number;
  totalAgentCount: number;
  knowledgeCount: number;
  toolCount: number;
  tokenTrend: Array<{ date: string; value: number }>;
  agentUsage: Array<{ name: string; tokens: number }>;
  agentDistribution: Array<{ name: string; value: number }>;
}

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
  time: string;
}

export interface ChatSession {
  id: string;
  sessionId: string;
  title: string;
  messages: string;
  messageCount: number;
  createTime: string;
  updateTime: string;
}

export interface User {
  id: number;
  username: string;
  password: string;
  avatar: string;
  admin: number;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface LoginLog {
  id: number;
  userId: number;
  username: string;
  ip: string;
  loginTime: string;
  status: "success" | "fail";
  message: string;
}

export interface OperationLog {
  id: number;
  userId: number;
  username: string;
  module: string;
  action: string;
  description: string;
  ip: string;
  createTime: string;
}
