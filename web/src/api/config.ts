import { get, post, put, del, patch } from "@/utils/request";
import type { ApiConfig, PaginatedResponse, PaginationParams } from "@/types";
import { CONFIG } from "./paths";

export function getConfigList(
  params: PaginationParams & { keyword?: string; status?: string },
): Promise<PaginatedResponse<ApiConfig>> {
  return get<PaginatedResponse<ApiConfig>>(CONFIG.LIST, params);
}

export function getConfigByName(name: string): Promise<ApiConfig[]> {
  return get<ApiConfig[]>(CONFIG.BY_NAME(name));
}

export function getConfigByIds(ids: string): Promise<ApiConfig> {
  return get<ApiConfig>(CONFIG.BY_ID(ids));
}

export function createConfig(data: Partial<ApiConfig>): Promise<ApiConfig> {
  return post<ApiConfig>(CONFIG.CREATE, data);
}

export function updateConfig(
  id: string,
  data: Partial<ApiConfig>,
): Promise<ApiConfig> {
  return put<ApiConfig>(CONFIG.UPDATE(id), data);
}

export function deleteConfig(id: string): Promise<void> {
  return del(CONFIG.DELETE(id)).then(() => {});
}

export function toggleConfigStatus(
  id: string,
  status: "active" | "inactive",
): Promise<void> {
  return patch(CONFIG.TOGGLE_STATUS(id), { status }).then(() => {});
}

export function getAllConfigs(): Promise<ApiConfig[]> {
  return get<ApiConfig[]>(CONFIG.ALL);
}

export function getConfigOptions(): Promise<
  Array<{ id: string; name: string }>
> {
  return get<Array<{ id: string; name: string }>>(CONFIG.OPTIONS);
}
