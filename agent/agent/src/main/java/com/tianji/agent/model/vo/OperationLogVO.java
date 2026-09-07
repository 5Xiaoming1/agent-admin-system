package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tianji.agent.model.entity.OperationLog;
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
@Schema(description = "操作日志视图对象")
public class OperationLogVO {

    @Schema(description = "日志ID")
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

    @Schema(description = "操作时间")
    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    public static OperationLogVO fromEntity(OperationLog entity) {
        return OperationLogVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .module(entity.getModule())
                .action(entity.getAction())
                .description(entity.getDescription())
                .ip(entity.getIp())
                .createTime(entity.getCreateTime())
                .build();
    }
}