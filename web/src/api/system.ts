import { get, post, put, del, patch, instance } from "@/utils/request";
import type {
  User,
  LoginLog,
  OperationLog,
  PaginatedResponse,
  PaginationParams,
} from "@/types";
import { SYSTEM } from "./paths";

// ==================== 用户管理 ====================

export function getUserProfile(): Promise<User> {
  return get<User>(SYSTEM.USER_PROFILE);
}

export function changePassword(data: {
  oldPassword: string;
  newPassword: string;
}): Promise<void> {
  return post(SYSTEM.USER_CHANGE_PWD, data).then(() => {});
}

export function updateUserProfile(data: {
  username: string;
  avatar?: string;
}): Promise<User> {
  return put<User>(SYSTEM.USER_PROFILE, data);
}

export function getUserList(
  params: PaginationParams & { keyword?: string; status?: string },
): Promise<PaginatedResponse<User>> {
  return get<PaginatedResponse<User>>(SYSTEM.USER_LIST, params);
}

export function getUserById(id: number): Promise<User> {
  return get<User>(SYSTEM.USER_DETAIL(id));
}

export function createUser(
  data: Omit<User, "id" | "createTime" | "updateTime">,
): Promise<User> {
  return post<User>(SYSTEM.USER_CREATE, data);
}

export function updateUser(
  id: number,
  data: Partial<Omit<User, "id" | "createTime" | "updateTime">>,
): Promise<User> {
  return put<User>(SYSTEM.USER_UPDATE(id), data);
}

export function deleteUser(id: number): Promise<void> {
  return del(SYSTEM.USER_DELETE(id)).then(() => {});
}

export function toggleUserStatus(id: number, status: 0 | 1): Promise<void> {
  return patch(SYSTEM.USER_TOGGLE_STATUS(id), { status }).then(() => {});
}

export function uploadAvatar(file: File): Promise<string> {
  const formData = new FormData();
  formData.append("file", file);
  return instance.post(SYSTEM.USER_UPLOAD_AVATAR, formData) as Promise<string>;
}

// ==================== 登录日志 ====================

export function getLoginLogList(
  params: PaginationParams & { keyword?: string; status?: string },
): Promise<PaginatedResponse<LoginLog>> {
  return get<PaginatedResponse<LoginLog>>(SYSTEM.LOGIN_LOG_LIST, params);
}

export function deleteLoginLog(ids: number | number[]): Promise<void> {
  const idsArray = Array.isArray(ids) ? ids : [ids];
  return post(SYSTEM.LOGIN_LOG_DELETE, { ids: idsArray }).then(() => {});
}

export function deleteAllLoginLogs(): Promise<void> {
  return del(SYSTEM.LOGIN_LOG_DELETE_ALL).then(() => {});
}

// ==================== 操作日志 ====================

export function getOperationLogList(
  params: PaginationParams & { keyword?: string; module?: string },
): Promise<PaginatedResponse<OperationLog>> {
  return get<PaginatedResponse<OperationLog>>(
    SYSTEM.OPERATION_LOG_LIST,
    params,
  );
}

export function deleteOperationLog(ids: number | number[]): Promise<void> {
  const idsArray = Array.isArray(ids) ? ids : [ids];
  return post(SYSTEM.OPERATION_LOG_DELETE, { ids: idsArray }).then(() => {});
}

export function deleteAllOperationLogs(): Promise<void> {
  return del(SYSTEM.OPERATION_LOG_DELETE_ALL).then(() => {});
}
