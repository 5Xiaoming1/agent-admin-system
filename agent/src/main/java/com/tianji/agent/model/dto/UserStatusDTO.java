package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户状态切换请求对象")
public class UserStatusDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "目标状态：1-启用，0-禁用", example = "1")
    private Integer status;
}