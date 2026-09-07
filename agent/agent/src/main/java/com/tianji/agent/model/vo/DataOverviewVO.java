package com.tianji.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘概览数据视图对象")
public class DataOverviewVO {

    @Schema(description = "Token 总用量")
    private Long totalTokens;

    @Schema(description = "已启用智能体数量")
    private Long activeAgentCount;

    @Schema(description = "智能体总数量")
    private Long totalAgentCount;

    @Schema(description = "知识库数量")
    private Long knowledgeCount;

    @Schema(description = "工具数量")
    private Long toolCount;

    @Schema(description = "Token 用量趋势数据")
    private List<TokenTrendVO> tokenTrend;

    @Schema(description = "Agent 用量排行数据")
    private List<AgentUsageVO> agentUsage;

    @Schema(description = "Agent 类型分布数据")
    private List<AgentDistributionVO> agentDistribution;
}