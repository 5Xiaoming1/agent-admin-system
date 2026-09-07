package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.dto.ToolsDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.ToolsVO;
import com.tianji.agent.service.ToolsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工具管理控制器
 * <p>
 * 处理工具管理的 HTTP 请求，作为 Controller 层负责：
 * <ul>
 *   <li>分页查询工具列表（支持关键词搜索、类型和状态筛选）</li>
 *   <li>按 ID 查询工具详情</li>
 *   <li>新增工具</li>
 *   <li>修改工具</li>
 *   <li>删除工具（逻辑删除）</li>
 *   <li>切换工具启用/禁用状态</li>
 *   <li>查询全部已启用工具</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/tools")
@Tag(name = "工具管理", description = "工具管理相关接口")
@Slf4j
@AllArgsConstructor
public class ToolsController {

    /** 工具管理服务 */
    private final ToolsService toolsService;

    /**
     * 分页查询工具列表
     * <p>
     * 支持按工具名称关键词模糊搜索、按类型和状态筛选，可单独或组合使用。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  工具名称关键词（可选）
     * @param type     工具类型筛选（可选），api/function/builtin
     * @param status   状态筛选（可选），active/inactive
     * @return 统一响应结果，data 包含工具列表和分页信息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询工具列表", description = "支持按名称模糊搜索、按类型和状态筛选")
    public Result<PageVO<ToolsVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        PageVO<ToolsVO> result = toolsService.queryPage(page, pageSize, keyword, type, status);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询工具
     *
     * @param id 工具 ID
     * @return 统一响应结果，data 包含工具详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工具", description = "返回指定ID的工具详情")
    public Result<ToolsVO> getById(@PathVariable Long id) {
        ToolsVO result = toolsService.queryById(id);
        return Result.success(result);
    }

    /**
     * 新增工具
     * <p>
     * 创建新的工具记录，默认状态为启用。
     * </p>
     *
     * @param dto 工具请求参数
     * @return 统一响应结果，data 包含新建的工具信息
     */
    @PostMapping("/create")
    @Operation(summary = "新增工具", description = "创建新的工具记录，默认状态为启用")
    public Result<ToolsVO> create(@Valid @RequestBody ToolsDTO dto) {
        ToolsVO result = toolsService.create(dto);
        return Result.success(result);
    }

    /**
     * 修改工具
     * <p>
     * 根据 ID 更新工具的名称、描述、类型、代码、接口地址和参数配置。
     * </p>
     *
     * @param id  工具 ID
     * @param dto 工具请求参数
     * @return 统一响应结果，data 包含更新后的工具信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改工具", description = "根据ID更新工具的名称、描述、类型、代码、接口地址和参数配置")
    public Result<ToolsVO> update(@PathVariable Long id, @Valid @RequestBody ToolsDTO dto) {
        ToolsVO result = toolsService.update(id, dto);
        return Result.success(result);
    }

    /**
     * 删除工具
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 工具 ID
     * @return 统一响应结果，删除成功
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除工具", description = "逻辑删除指定工具")
    public Result<Void> delete(@PathVariable Long id) {
        toolsService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换工具状态
     * <p>
     * 将工具在启用（active）和禁用（inactive）之间切换。
     * </p>
     *
     * @param id  工具 ID
     * @param dto 状态切换请求参数
     * @return 统一响应结果，切换成功
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "切换工具状态", description = "切换工具的启用/禁用状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        toolsService.toggleStatus(id, dto);
        return Result.success();
    }

    /**
     * 查询全部工具
     * <p>
     * 不分页，返回所有已启用的工具，供下拉选择等场景使用。
     * </p>
     *
     * @return 统一响应结果，data 包含全部已启用工具列表
     */
    @GetMapping("/all")
    @Operation(summary = "查询全部工具", description = "不分页，返回所有已启用的工具")
    public Result<List<ToolsVO>> getAll() {
        List<ToolsVO> result = toolsService.queryAll();
        return Result.success(result);
    }
}