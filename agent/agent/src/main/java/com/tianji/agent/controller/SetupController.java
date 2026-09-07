package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.dto.SetupDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.SetupOptionVO;
import com.tianji.agent.model.vo.SetupVO;
import com.tianji.agent.service.SetupService;
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
 * 配置管理控制器
 * <p>
 * 处理配置管理的 HTTP 请求，作为 Controller 层负责：
 * <ul>
 *   <li>分页查询配置列表（支持关键词搜索和状态筛选）</li>
 *   <li>按名称模糊查询配置</li>
 *   <li>按 ID 查询配置详情</li>
 *   <li>新增配置</li>
 *   <li>修改配置</li>
 *   <li>删除配置（逻辑删除）</li>
 *   <li>切换配置启用/禁用状态</li>
 *   <li>查询全部已启用配置</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/setup")
@Tag(name = "配置管理", description = "配置管理相关接口")
@Slf4j
@AllArgsConstructor
public class SetupController {

    /** 配置管理服务 */
    private final SetupService setupService;

    /**
     * 分页查询配置列表
     * <p>
     * 支持按配置名称关键词模糊搜索和按状态筛选，可单独或组合使用。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  配置名称关键词（可选）
     * @param status   状态筛选（可选），active 或 inactive
     * @return 统一响应结果，data 包含配置列表和分页信息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询配置列表", description = "支持按名称模糊搜索和按状态筛选")
    public Result<PageVO<SetupVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        PageVO<SetupVO> result = setupService.queryPage(page, pageSize, keyword, status);
        return Result.success(result);
    }

    /**
     * 根据配置名称模糊查询
     * <p>
     * 按配置名称进行模糊匹配，返回匹配的配置列表。
     * </p>
     *
     * @param name 配置名称关键词
     * @return 统一响应结果，data 包含匹配的配置列表
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称模糊查询配置", description = "按配置名称模糊匹配，返回匹配的配置列表")
    public Result<List<SetupVO>> getByName(@PathVariable String name) {
        List<SetupVO> result = setupService.queryByName(name);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询配置
     *
     * @param id 配置 ID
     * @return 统一响应结果，data 包含配置详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配置", description = "返回指定ID的配置详情")
    public Result<SetupVO> getById(@PathVariable Long id) {
        SetupVO result = setupService.queryById(id);
        return Result.success(result);
    }

    /**
     * 新增配置
     * <p>
     * 创建新的配置记录，默认状态为启用。
     * </p>
     *
     * @param dto 配置请求参数
     * @return 统一响应结果，data 包含新建的配置信息
     */
    @PostMapping("/create")
    @Operation(summary = "新增配置", description = "创建新的配置记录，默认状态为启用")
    public Result<SetupVO> create(@Valid @RequestBody SetupDTO dto) {
        SetupVO result = setupService.create(dto);
        return Result.success(result);
    }

    /**
     * 修改配置
     * <p>
     * 根据 ID 更新配置的名称、API 密钥和模型地址。
     * </p>
     *
     * @param id  配置 ID
     * @param dto 配置请求参数
     * @return 统一响应结果，data 包含更新后的配置信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改配置", description = "根据ID更新配置的名称、API密钥和模型地址")
    public Result<SetupVO> update(@PathVariable Long id, @Valid @RequestBody SetupDTO dto) {
        SetupVO result = setupService.update(id, dto);
        return Result.success(result);
    }

    /**
     * 删除配置
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 配置 ID
     * @return 统一响应结果，删除成功
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除配置", description = "逻辑删除指定配置")
    public Result<Void> delete(@PathVariable Long id) {
        setupService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换配置状态
     * <p>
     * 将配置在启用（active）和禁用（inactive）之间切换。
     * </p>
     *
     * @param id  配置 ID
     * @param dto 状态切换请求参数
     * @return 统一响应结果，切换成功
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "切换配置状态", description = "切换配置的启用/禁用状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        setupService.toggleStatus(id, dto);
        return Result.success();
    }

    /**
     * 查询全部配置
     * <p>
     * 不分页，返回所有已启用的配置，供下拉选择等场景使用。
     * </p>
     *
     * @return 统一响应结果，data 包含全部已启用配置列表
     */
    @GetMapping("/all")
    @Operation(summary = "查询全部配置", description = "不分页，返回所有已启用的配置")
    public Result<List<SetupVO>> getAll() {
        List<SetupVO> result = setupService.queryAll();
        return Result.success(result);
    }

    /**
     * 查询配置选项列表（下拉选择专用）
     * <p>
     * 仅返回 id 和 name 两个字段，不包含 apiKey 等敏感信息，
     * 用于 Agent 创建/编辑时配置下拉选择框的数据源。
     * 无需解密 apiKey，性能更优。
     * </p>
     *
     * @return 统一响应结果，data 包含配置选项列表（id + name）
     */
    @GetMapping("/options")
    @Operation(summary = "查询配置选项列表", description = "仅返回 id 和 name，用于下拉选择框，不包含敏感字段")
    public Result<List<SetupOptionVO>> getOptions() {
        List<SetupOptionVO> result = setupService.queryOptions();
        return Result.success(result);
    }
}