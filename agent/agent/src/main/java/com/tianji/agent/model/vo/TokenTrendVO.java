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
@Schema(description = "Token 用量趋势视图对象")
public class TokenTrendVO {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "Token 用量值")
    private Long value;
}