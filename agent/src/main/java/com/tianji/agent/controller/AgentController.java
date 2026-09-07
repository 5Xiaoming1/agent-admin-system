package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.model.dto.AgentDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.vo.AgentVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.AgentService;
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
 * 智能体管理控制器
 * <p>
 * 处理智能体管理的 HTTP 请求，作为 Controller 层负责：
 * <ul>
 *   <li>分页查询智能体列表（支持关键词搜索、类型和状态筛选）</li>
 *   <li>按 ID 查询智能体详情</li>
 *   <li>新增智能体</li>
 *   <li>修改智能体</li>
 *   <li>删除智能体（逻辑删除）</li>
 *   <li>切换智能体启用/禁用状态</li>
 *   <li>查询全部已启用智能体</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/agents")
@Tag(name = "智能体管理", description = "智能体管理相关接口")
@Slf4j
@AllArgsConstructor
public class AgentController {

    /** 智能体管理服务 */
    private final AgentService agentService;

    /**
     * 分页查询智能体列表
     * <p>
     * 支持按智能体名称关键词模糊搜索、按类型和状态筛选，可单独或组合使用。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  智能体名称关键词（可选）
     * @param type     智能体类型筛选（可选），chat/task/workflow
     * @param status   状态筛选（可选），active/inactive
     * @return 统一响应结果，data 包含智能体列表和分页信息
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询智能体列表", description = "支持按名称模糊搜索、按类型和状态筛选")
    public Result<PageVO<AgentVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        PageVO<AgentVO> result = agentService.queryPage(page, pageSize, keyword, type, status);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询智能体
     *
     * @param id 智能体 ID
     * @return 统一响应结果，data 包含智能体详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询智能体", description = "返回指定ID的智能体详情")
    public Result<AgentVO> getById(@PathVariable Long id) {
        AgentVO result = agentService.queryById(id);
        return Result.success(result);
    }

    /**
     * 新增智能体
     * <p>
     * 创建新的智能体记录，默认状态为启用。
     * </p>
     *
     * @param dto 智能体请求参数
     * @return 统一响应结果，data 包含新建的智能体信息
     */
    @PostMapping("/create")
    @Operation(summary = "新增智能体", description = "创建新的智能体记录，默认状态为启用")
    public Result<AgentVO> create(@Valid @RequestBody AgentDTO dto) {
        AgentVO result = agentService.create(dto);
        return Result.success(result);
    }

    /**
     * 修改智能体
     * <p>
     * 根据 ID 更新智能体的名称、描述、类型、关联配置、关联知识库、关联工具和 Token 用量。
     * </p>
     *
     * @param id  智能体 ID
     * @param dto 智能体请求参数
     * @return 统一响应结果，data 包含更新后的智能体信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改智能体", description = "根据ID更新智能体的名称、描述、类型、关联配置、关联知识库、关联工具和Token用量")
    public Result<AgentVO> update(@PathVariable Long id, @Valid @RequestBody AgentDTO dto) {
        AgentVO result = agentService.update(id, dto);
        return Result.success(result);
    }

    /**
     * 删除智能体
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 智能体 ID
     * @return 统一响应结果，删除成功
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除智能体", description = "逻辑删除指定智能体")
    public Result<Void> delete(@PathVariable Long id) {
        agentService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换智能体状态
     * <p>
     * 将智能体在启用（active）和禁用（inactive）之间切换。
     * </p>
     *
     * @param id  智能体 ID
     * @param dto 状态切换请求参数
     * @return 统一响应结果，切换成功
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "切换智能体状态", description = "切换智能体的启用/禁用状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        agentService.toggleStatus(id, dto);
        return Result.success();
    }

    /**
     * 查询全部智能体
     * <p>
     * 不分页，返回所有已启用的智能体，供下拉选择等场景使用。
     * </p>
     *
     * @return 统一响应结果，data 包含全部已启用智能体列表
     */
    @GetMapping("/all")
    @Operation(summary = "查询全部智能体", description = "不分页，返回所有已启用的智能体")
    public Result<List<AgentVO>> getAll() {
        List<AgentVO> result = agentService.queryAll();
        return Result.success(result);
    }
}