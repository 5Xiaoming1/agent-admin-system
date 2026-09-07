package com.tianji.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 类型分布视图对象")
public class AgentDistributionVO {

    @Schema(description = "Agent 类型名称")
    private String name;

    @Schema(description = "该类型 Agent 的数量")
    private Long value;
}