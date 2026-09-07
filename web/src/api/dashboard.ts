import { get } from "@/utils/request";
import type { DashboardData } from "@/types";
import { DASHBOARD } from "./paths";

export function getDashboardOverview(): Promise<DashboardData> {
  return get<DashboardData>(DASHBOARD.OVERVIEW);
}

export function getTokenTrend(): Promise<
  Array<{ date: string; value: number }>
> {
  return get<Array<{ date: string; value: number }>>(DASHBOARD.TOKEN_TREND);
}

export function getAgentUsage(): Promise<
  Array<{ name: string; tokens: number }>
> {
  return get<Array<{ name: string; tokens: number }>>(DASHBOARD.AGENT_USAGE);
}

export function getAgentDistribution(): Promise<
  Array<{ name: string; value: number }>
> {
  return get<Array<{ name: string; value: number }>>(
    DASHBOARD.AGENT_DISTRIBUTION,
  );
}
