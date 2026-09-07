/**
 * 项目全局配置
 *
 * 集中管理前后端地址、端口等配置项
 * 修改后端地址或端口时，只需在此文件中修改即可
 */

export const CONFIG = {
  /** 前端开发服务器端口 */
  FRONTEND_PORT: 8080,

  /** 后端 API 服务地址 */
  BACKEND_URL: "http://localhost:8081",

  /** API 请求前缀 */
  API_PREFIX: "/api",

  /** 请求超时时间（毫秒） */
  REQUEST_TIMEOUT: 10000,
} as const;
