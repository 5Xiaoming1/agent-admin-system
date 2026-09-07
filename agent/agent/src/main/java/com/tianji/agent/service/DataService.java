package com.tianji.agent.service;

import com.tianji.agent.model.vo.AgentDistributionVO;
import com.tianji.agent.model.vo.AgentUsageVO;
import com.tianji.agent.model.vo.DataOverviewVO;
import com.tianji.agent.model.vo.TokenTrendVO;

import java.util.List;

/**
 * 数据看板服务接口
 * <p>
 * 定义数据看板的核心业务逻辑，提供仪表盘概览数据、
 * Token 用量趋势、Agent 用量排行和 Agent 类型分布等统计查询。
 * </p>
 */
public interface DataService {

    /**
     * 获取仪表盘概览数据
     * <p>
     * 聚合展示系统的核心统计指标，包括 Token 总用量、智能体数量、
     * 知识库数量、工具数量，以及 Token 趋势、Agent 用量排行和类型分布。
     * </p>
     *
     * @return 概览数据 VO
     */
    DataOverviewVO getOverview();

    /**
     * 获取 Token 用量趋势
     *
     * @return Token 用量趋势数据列表
     */
    List<TokenTrendVO> getTokenTrend();

    /**
     * 获取 Agent 用量排行
     * <p>
     * 按 Token 用量降序排列，返回所有 Agent 的用量排行。
     * </p>
     *
     * @return Agent 用量排行列表
     */
    List<AgentUsageVO> getAgentUsage();

    /**
     * 获取 Agent 类型分布
     * <p>
     * 按 Agent 类型分组统计数量。
     * </p>
     *
     * @return Agent 类型分布列表
     */
    List<AgentDistributionVO> getAgentDistribution();
}