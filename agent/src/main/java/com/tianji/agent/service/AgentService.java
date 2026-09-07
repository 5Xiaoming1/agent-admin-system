package com.tianji.agent.service;

import com.tianji.agent.model.dto.AgentDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.vo.AgentVO;
import com.tianji.agent.model.vo.PageVO;

import java.util.List;

/**
 * 智能体管理服务接口
 * <p>
 * 定义智能体管理的核心业务逻辑，包括智能体的增删改查和状态切换。
 * 由 {@link com.tianji.agent.service.impl.AgentServiceImpl} 提供具体实现。
 * </p>
 */
public interface AgentService {

    /**
     * 分页查询智能体列表
     * <p>
     * 支持按智能体名称关键词模糊搜索、按类型和状态筛选。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  智能体名称关键词（可选），为空则查询全部
     * @param type     智能体类型筛选（可选），为空则查询全部
     * @param status   状态筛选（可选），active/inactive，为空则查询全部
     * @return 分页结果，包含智能体列表和分页信息
     */
    PageVO<AgentVO> queryPage(Integer page, Integer pageSize, String keyword, String type, String status);

    /**
     * 根据 ID 查询智能体
     *
     * @param id 智能体 ID
     * @return 智能体详情，不存在时抛出异常
     */
    AgentVO queryById(Long id);

    /**
     * 新增智能体
     * <p>
     * 创建新的智能体记录，默认状态为启用。
     * </p>
     *
     * @param dto 智能体请求参数
     * @return 创建后的智能体信息
     */
    AgentVO create(AgentDTO dto);

    /**
     * 修改智能体
     * <p>
     * 根据 ID 更新智能体的名称、描述、类型、关联配置、关联知识库、关联工具和 Token 用量。
     * </p>
     *
     * @param id  智能体 ID
     * @param dto 智能体请求参数
     * @return 更新后的智能体信息
     */
    AgentVO update(Long id, AgentDTO dto);

    /**
     * 删除智能体
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 智能体 ID
     */
    void delete(Long id);

    /**
     * 切换智能体状态
     * <p>
     * 将智能体在启用和禁用之间切换。
     * </p>
     *
     * @param id  智能体 ID
     * @param dto 状态切换请求参数
     * @return 更新后的状态值
     */
    String toggleStatus(Long id, StatusDTO dto);

    /**
     * 查询全部智能体
     * <p>
     * 不分页，返回所有已启用的智能体，供下拉选择等场景使用。
     * </p>
     *
     * @return 全部智能体列表
     */
    List<AgentVO> queryAll();
}