package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.agent.mapper.AgentMapper;
import com.tianji.agent.mapper.KnowledgeMapper;
import com.tianji.agent.mapper.ToolsMapper;
import com.tianji.agent.model.entity.Agent;
import com.tianji.agent.model.vo.AgentDistributionVO;
import com.tianji.agent.model.vo.AgentUsageVO;
import com.tianji.agent.model.vo.DataOverviewVO;
import com.tianji.agent.model.vo.TokenTrendVO;
import com.tianji.agent.service.DataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据看板服务实现
 * <p>
 * 实现 {@link DataService} 接口，完成数据看板的完整业务流程：
 * 仪表盘概览、Token 趋势、Agent 用量排行和 Agent 类型分布。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>totalTokens — 所有智能体的 Token 用量总和</li>
 *   <li>activeAgentCount — 状态为启用的智能体数量</li>
 *   <li>totalAgentCount — 所有智能体数量（含已禁用）</li>
 *   <li>knowledgeCount — 知识库总数量</li>
 *   <li>toolCount — 工具总数量</li>
 *   <li>tokenTrend — Token 用量趋势，基于当前日期和总 Token 量</li>
 *   <li>agentUsage — 按 Token 用量降序排列的 Agent 排行</li>
 *   <li>agentDistribution — 按类型分组统计 Agent 数量</li>
 * </ul>
 */
@Service
@Slf4j
public class DataServiceImpl implements DataService {

    /** 智能体持久层 Mapper */
    private final AgentMapper agentMapper;

    /** 知识库持久层 Mapper */
    private final KnowledgeMapper knowledgeMapper;

    /** 工具持久层 Mapper */
    private final ToolsMapper toolsMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DataServiceImpl(AgentMapper agentMapper,
                           KnowledgeMapper knowledgeMapper,
                           ToolsMapper toolsMapper) {
        this.agentMapper = agentMapper;
        this.knowledgeMapper = knowledgeMapper;
        this.toolsMapper = toolsMapper;
    }

    @Override
    public DataOverviewVO getOverview() {
        log.info("获取仪表盘概览数据");

        List<Agent> allAgents = agentMapper.selectList(null);

        long totalTokens = allAgents.stream()
                .mapToLong(agent -> agent.getTokens() != null ? agent.getTokens() : 0L)
                .sum();

        long activeAgentCount = allAgents.stream()
                .filter(agent -> agent.getStatus() != null && agent.getStatus() == 1)
                .count();

        long totalAgentCount = allAgents.size();

        long knowledgeCount = knowledgeMapper.selectCount(null);

        long toolCount = toolsMapper.selectCount(null);

        List<TokenTrendVO> tokenTrend = getTokenTrend();
        List<AgentUsageVO> agentUsage = getAgentUsage();
        List<AgentDistributionVO> agentDistribution = getAgentDistribution();

        return DataOverviewVO.builder()
                .totalTokens(totalTokens)
                .activeAgentCount(activeAgentCount)
                .totalAgentCount(totalAgentCount)
                .knowledgeCount(knowledgeCount)
                .toolCount(toolCount)
                .tokenTrend(tokenTrend)
                .agentUsage(agentUsage)
                .agentDistribution(agentDistribution)
                .build();
    }

    @Override
    public List<TokenTrendVO> getTokenTrend() {
        log.info("获取 Token 用量趋势");

        List<Agent> allAgents = agentMapper.selectList(null);
        long totalTokens = allAgents.stream()
                .mapToLong(agent -> agent.getTokens() != null ? agent.getTokens() : 0L)
                .sum();

        String today = LocalDate.now().format(DATE_FORMATTER);

        return List.of(TokenTrendVO.builder()
                .date(today)
                .value(totalTokens)
                .build());
    }

    @Override
    public List<AgentUsageVO> getAgentUsage() {
        log.info("获取 Agent 用量排行");

        List<Agent> allAgents = agentMapper.selectList(null);

        return allAgents.stream()
                .map(agent -> AgentUsageVO.builder()
                        .name(agent.getName())
                        .tokens(agent.getTokens() != null ? agent.getTokens() : 0L)
                        .build())
                .sorted((a, b) -> Long.compare(b.getTokens(), a.getTokens()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentDistributionVO> getAgentDistribution() {
        log.info("获取 Agent 类型分布");

        List<Agent> allAgents = agentMapper.selectList(null);

        Map<String, Long> typeCountMap = allAgents.stream()
                .collect(Collectors.groupingBy(
                        agent -> agent.getType() != null ? agent.getType() : "unknown",
                        Collectors.counting()
                ));

        return typeCountMap.entrySet().stream()
                .map(entry -> AgentDistributionVO.builder()
                        .name(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}