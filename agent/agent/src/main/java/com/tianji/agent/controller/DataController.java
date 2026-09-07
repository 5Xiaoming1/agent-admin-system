package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.vo.AgentDistributionVO;
import com.tianji.agent.model.vo.AgentUsageVO;
import com.tianji.agent.model.vo.DataOverviewVO;
import com.tianji.agent.model.vo.TokenTrendVO;
import com.tianji.agent.service.DataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据看板控制器
 * <p>
 * 处理数据看板的 HTTP 请求，作为 Controller 层负责：
 * <ul>
 *   <li>获取仪表盘概览数据（含 Token 趋势、Agent 用量排行、类型分布）</li>
 *   <li>获取 Token 用量趋势</li>
 *   <li>获取 Agent 用量排行</li>
 *   <li>获取 Agent 类型分布数据</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/data")
@Tag(name = "数据看板", description = "数据看板相关接口")
@Slf4j
@AllArgsConstructor
public class DataController {

    /** 数据看板服务 */
    private final DataService dataService;

    /**
     * 获取仪表盘概览数据
     * <p>
     * 一次性返回仪表盘所需的所有统计数据，包括 Token 总用量、
     * 智能体数量、知识库数量、工具数量，以及趋势、排行和分布数据。
     * </p>
     *
     * @return 统一响应结果，data 包含概览数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取仪表盘概览数据", description = "返回仪表盘所需的所有统计数据，包括 Token 趋势、Agent 用量排行和类型分布")
    public Result<DataOverviewVO> overview() {
        DataOverviewVO result = dataService.getOverview();
        return Result.success(result);
    }

    /**
     * 获取 Token 用量趋势
     * <p>
     * 返回 Token 用量趋势数据，按日期展示。
     * </p>
     *
     * @return 统一响应结果，data 包含 Token 用量趋势数据列表
     */
    @GetMapping("/token-trend")
    @Operation(summary = "获取 Token 用量趋势", description = "返回 Token 用量趋势数据，按日期展示")
    public Result<List<TokenTrendVO>> tokenTrend() {
        List<TokenTrendVO> result = dataService.getTokenTrend();
        return Result.success(result);
    }

    /**
     * 获取 Agent 用量排行
     * <p>
     * 按 Token 用量降序排列，返回所有 Agent 的用量排行。
     * </p>
     *
     * @return 统一响应结果，data 包含 Agent 用量排行列表
     */
    @GetMapping("/agent-usage")
    @Operation(summary = "获取 Agent 用量排行", description = "按 Token 用量降序排列，返回所有 Agent 的用量排行")
    public Result<List<AgentUsageVO>> agentUsage() {
        List<AgentUsageVO> result = dataService.getAgentUsage();
        return Result.success(result);
    }

    /**
     * 获取 Agent 类型分布数据
     * <p>
     * 按 Agent 类型分组统计数量，用于饼图或柱状图展示。
     * </p>
     *
     * @return 统一响应结果，data 包含 Agent 类型分布列表
     */
    @GetMapping("/agent-distribution")
    @Operation(summary = "获取 Agent 分布数据", description = "按 Agent 类型分组统计数量，用于饼图展示")
    public Result<List<AgentDistributionVO>> agentDistribution() {
        List<AgentDistributionVO> result = dataService.getAgentDistribution();
        return Result.success(result);
    }
}