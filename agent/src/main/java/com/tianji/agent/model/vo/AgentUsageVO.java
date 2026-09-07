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
@Schema(description = "Agent 用量排行视图对象")
public class AgentUsageVO {

    @Schema(description = "Agent 名称")
    private String name;

    @Schema(description = "Token 用量")
    private Long tokens;
}