package com.tianji.agent.service;

import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.dto.ToolsDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.ToolsVO;

import java.util.List;

/**
 * 工具管理服务接口
 * <p>
 * 定义工具管理的核心业务逻辑，包括工具的增删改查和状态切换。
 * 由 {@link com.tianji.agent.service.impl.ToolsServiceImpl} 提供具体实现。
 * </p>
 */
public interface ToolsService {

    /**
     * 分页查询工具列表
     * <p>
     * 支持按工具名称关键词模糊搜索、按类型和状态筛选。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  工具名称关键词（可选），为空则查询全部
     * @param type     工具类型筛选（可选），api/function/builtin，为空则查询全部
     * @param status   状态筛选（可选），active/inactive，为空则查询全部
     * @return 分页结果，包含工具列表和分页信息
     */
    PageVO<ToolsVO> queryPage(Integer page, Integer pageSize, String keyword, String type, String status);

    /**
     * 根据 ID 查询工具
     *
     * @param id 工具 ID
     * @return 工具详情，不存在时抛出异常
     */
    ToolsVO queryById(Long id);

    /**
     * 新增工具
     * <p>
     * 创建新的工具记录，默认状态为启用。
     * </p>
     *
     * @param dto 工具请求参数
     * @return 创建后的工具信息
     */
    ToolsVO create(ToolsDTO dto);

    /**
     * 修改工具
     * <p>
     * 根据 ID 更新工具的名称、描述、类型、代码、接口地址和参数配置。
     * </p>
     *
     * @param id  工具 ID
     * @param dto 工具请求参数
     * @return 更新后的工具信息
     */
    ToolsVO update(Long id, ToolsDTO dto);

    /**
     * 删除工具
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 工具 ID
     */
    void delete(Long id);

    /**
     * 切换工具状态
     * <p>
     * 将工具在启用和禁用之间切换。
     * </p>
     *
     * @param id  工具 ID
     * @param dto 状态切换请求参数
     * @return 更新后的状态值
     */
    String toggleStatus(Long id, StatusDTO dto);

    /**
     * 查询全部工具
     * <p>
     * 不分页，返回所有已启用的工具，供下拉选择等场景使用。
     * </p>
     *
     * @return 全部工具列表
     */
    List<ToolsVO> queryAll();
}