import { get, post, put, del, patch } from "@/utils/request";
import type { Tool, PaginatedResponse, PaginationParams } from "@/types";
import { TOOL } from "./paths";

export function getToolList(
  params: PaginationParams & {
    keyword?: string;
    type?: string;
    status?: string;
  },
): Promise<PaginatedResponse<Tool>> {
  return get<PaginatedResponse<Tool>>(TOOL.LIST, params);
}

export function getToolById(id: string): Promise<Tool> {
  return get<Tool>(TOOL.DETAIL(id));
}

export function createTool(
  data: Omit<Tool, "id" | "status" | "createdAt" | "updatedAt">,
): Promise<Tool> {
  return post<Tool>(TOOL.CREATE, data);
}

export function updateTool(
  id: string,
  data: Partial<Omit<Tool, "id" | "createdAt" | "updatedAt">>,
): Promise<Tool> {
  return put<Tool>(TOOL.UPDATE(id), data);
}

export function deleteTool(id: string): Promise<void> {
  return del(TOOL.DELETE(id)).then(() => {});
}

export function toggleToolStatus(id: string, status: string): Promise<void> {
  return patch(TOOL.TOGGLE_STATUS(id), { status }).then(() => {});
}

export function getAllTools(): Promise<Tool[]> {
  return get<Tool[]>(TOOL.ALL);
}
