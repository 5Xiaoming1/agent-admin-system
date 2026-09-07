package com.tianji.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
@Schema(description = "操作日志实体")
public class OperationLog {

    @Schema(description = "日志ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作IP地址")
    private String ip;

    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    @TableLogic
    private Integer isDeleted;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}