import { get, post, put, del, patch } from "@/utils/request";
import type { Agent, PaginatedResponse, PaginationParams } from "@/types";
import { AGENT } from "./paths";

export function getAgentList(
  params: PaginationParams & {
    keyword?: string;
    type?: string;
    status?: string;
  },
): Promise<PaginatedResponse<Agent>> {
  return get<PaginatedResponse<Agent>>(AGENT.LIST, params);
}

export function getAgentById(id: string): Promise<Agent> {
  return get<Agent>(AGENT.DETAIL(id));
}

export function createAgent(
  data: Omit<Agent, "id" | "createdAt" | "updatedAt" | "tokens">,
): Promise<Agent> {
  return post<Agent>(AGENT.CREATE, data);
}

export function updateAgent(
  id: string,
  data: Partial<Omit<Agent, "id" | "createdAt" | "updatedAt">>,
): Promise<Agent> {
  return put<Agent>(AGENT.UPDATE(id), data);
}

export function deleteAgent(id: string): Promise<void> {
  return del(AGENT.DELETE(id)).then(() => {});
}

export function toggleAgentStatus(
  id: string,
  status: "active" | "inactive",
): Promise<void> {
  return patch(AGENT.TOGGLE_STATUS(id), { status }).then(() => {});
}

export function getAllAgents(): Promise<Agent[]> {
  return get<Agent[]>(AGENT.ALL);
}
