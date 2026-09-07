import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import type { ApiResponse } from "@/types";
import { ElMessage } from "element-plus";
import { CONFIG } from "./config";

const instance: AxiosInstance = axios.create({
  baseURL: CONFIG.API_PREFIX,
  timeout: CONFIG.REQUEST_TIMEOUT,
});

instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

instance.interceptors.response.use(
  (response) => {
    // Blob 响应（文件下载/预览）直接返回，不做 JSON 解析
    if (response.config.responseType === "blob") {
      return response.data;
    }
    const res = response.data as ApiResponse;
    if (res.code !== 200) {
      ElMessage.error(res.message || "请求失败");
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    return res.data;
  },
  (error) => {
    const url = error.config?.url || "";
    const method = error.config?.method?.toUpperCase() || "";
    const fullPath = `${method} /api${url}`;
    if (error.response) {
      const { status, data } = error.response;
      switch (status) {
        case 401:
          // 仅在已登录状态下才清除 token 并跳转
          if (localStorage.getItem("token")) {
            localStorage.removeItem("token");
            localStorage.removeItem("userId");
            localStorage.removeItem("username");
            window.location.href = "/login";
          }
          // 登录接口本身的业务错误（如验证码错误）由调用方处理
          break;
        case 403:
          ElMessage.error(`${fullPath} — 拒绝访问`);
          break;
        case 500:
          ElMessage.error(`${fullPath} — 服务器错误`);
          break;
        default:
          ElMessage.error(`${fullPath} — 请求错误: ${status}`);
      }
    } else {
      ElMessage.error(`${fullPath} — 网络错误（后端未启动或接口不存在）`);
    }
    return Promise.reject(error);
  },
);

export async function get<T = any>(
  url: string,
  params?: Record<string, any>,
): Promise<T> {
  return instance.get<T>(url, { params });
}

export async function post<T = any>(
  url: string,
  data?: Record<string, any>,
): Promise<T> {
  return instance.post<T>(url, data);
}

export async function put<T = any>(
  url: string,
  data?: Record<string, any>,
): Promise<T> {
  return instance.put<T>(url, data);
}

export async function del<T = any>(url: string): Promise<T> {
  return instance.delete<T>(url);
}

export async function patch<T = any>(
  url: string,
  data?: Record<string, any>,
): Promise<T> {
  return instance.patch<T>(url, data);
}

export { instance };
export default instance;
