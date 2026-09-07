package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_log")
@Schema(description = "登录日志实体")
public class LoginLog {

    @Schema(description = "日志ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP地址")
    private String ip;

    @Schema(description = "登录状态：1-成功，0-失败")
    private Integer status;

    @Schema(description = "登录信息/失败原因")
    private String message;

    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}