package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求 DTO
 * <p>
 * 用于批量删除会话时接收前端提交的 ID 列表。
 * </p>
 */
@Data
@Schema(description = "批量删除请求参数")
public class BatchDeleteDTO {

    /** 要删除的会话记录 ID 列表 */
    @NotEmpty(message = "ID列表不能为空")
    @Schema(description = "会话记录ID集合", example = "[1, 2, 3]")
    private List<Long> ids;
}