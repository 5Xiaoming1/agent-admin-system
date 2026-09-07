package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态切换请求 DTO
 * <p>
 * 用于切换配置启用/禁用状态时接收前端提交的状态值。
 * </p>
 */
@Data
@Schema(description = "状态切换请求对象")
public class StatusDTO {

    /** 目标状态：active（启用）/ inactive（禁用） */
    @NotBlank(message = "状态不能为空")
    @Schema(description = "目标状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;
}