package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.LoginLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录日志视图对象")
public class LoginLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP地址")
    private String ip;

    @Schema(description = "登录状态：success-成功，fail-失败")
    private String status;

    @Schema(description = "登录信息/失败原因")
    private String message;

    @Schema(description = "登录时间")
    @JsonProperty("loginTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime loginTime;

    public static LoginLogVO fromEntity(LoginLog entity) {
        return LoginLogVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .ip(entity.getIp())
                .status(entity.getStatus() == 1 ? "success" : "fail")
                .message(entity.getMessage())
                .loginTime(entity.getLoginTime())
                .build();
    }
}