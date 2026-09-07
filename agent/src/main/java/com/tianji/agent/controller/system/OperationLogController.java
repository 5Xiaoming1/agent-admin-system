package com.tianji.agent.controller.system;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.dto.BatchDeleteDTO;
import com.tianji.agent.model.vo.OperationLogVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 * <p>
 * 提供操作日志的 RESTful API 接口，包括分页查询和逻辑删除操作。
 * </p>
 *
 * <p><b>接口路径：</b>{@code /api/system/operation-log}</p>
 *
 * <p><b>接口列表：</b></p>
 * <ul>
 *   <li>GET /api/system/operation-log/list - 分页查询操作日志</li>
 *   <li>POST /api/system/operation-log/delete - 删除操作日志（传递 ids 集合，传几个删几个）</li>
 *   <li>DELETE /api/system/operation-log/delete/all - 删除全部操作日志</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/system/operation-log")
@Tag(name = "操作日志", description = "操作日志相关接口")
@Slf4j
@AllArgsConstructor
public class OperationLogController {

    /** 操作日志服务 */
    private final OperationLogService operationLogService;

    @GetMapping("/list")
    @Operation(summary = "分页查询操作日志", description = "支持按用户名模糊搜索和操作模块筛选")
    public Result<PageVO<OperationLogVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module) {
        PageVO<OperationLogVO> result = operationLogService.queryPage(page, pageSize, keyword, module);
        return Result.success(result);
    }

    /**
     * 删除操作日志
     * <p>
     * 逻辑删除，将 is_deleted 字段设为 1。传递几个 ID 就删除几个。
     * </p>
     *
     * @param dto 删除请求参数，包含操作日志 ID 列表
     * @return 统一响应结果，删除成功
     */
    @PostMapping("/delete")
    @Operation(summary = "删除操作日志", description = "逻辑删除操作日志记录，传递 ids 集合，传几个删几个")
    public Result<Void> delete(@Valid @RequestBody BatchDeleteDTO dto) {
        operationLogService.deleteByIds(dto.getIds());
        return Result.success("删除成功", null);
    }

    /**
     * 删除全部操作日志
     * <p>
     * 逻辑删除所有操作日志记录，将 is_deleted 字段设为 1。
     * </p>
     *
     * @return 统一响应结果，删除成功
     */
    @DeleteMapping("/delete/all")
    @Operation(summary = "删除全部操作日志", description = "逻辑删除所有操作日志记录")
    public Result<Void> deleteAll() {
        operationLogService.deleteAll();
        return Result.success("删除成功", null);
    }
}