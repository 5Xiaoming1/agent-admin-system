package com.tianji.agent.service;

import com.tianji.agent.model.dto.SetupDTO;
import com.tianji.agent.model.dto.StatusDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.SetupOptionVO;
import com.tianji.agent.model.vo.SetupVO;

import java.util.List;

/**
 * 配置管理服务接口
 * <p>
 * 定义配置管理的核心业务逻辑，包括配置的增删改查和状态切换。
 * 由 {@link com.tianji.agent.service.impl.SetupServiceImpl} 提供具体实现。
 * </p>
 */
public interface SetupService {

    /**
     * 分页查询配置列表
     * <p>
     * 支持按配置名称关键词模糊搜索和按状态筛选。
     * </p>
     *
     * @param page     页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  配置名称关键词（可选），为空则查询全部
     * @param status   状态筛选（可选），active/inactive，为空则查询全部
     * @return 分页结果，包含配置列表和分页信息
     */
    PageVO<SetupVO> queryPage(Integer page, Integer pageSize, String keyword, String status);

    /**
     * 根据配置名称模糊查询
     * <p>
     * 按配置名称进行模糊匹配，返回匹配的配置列表。
     * </p>
     *
     * @param name 配置名称关键词
     * @return 匹配的配置列表
     */
    List<SetupVO> queryByName(String name);

    /**
     * 根据 ID 查询配置
     *
     * @param id 配置 ID
     * @return 配置详情，不存在时返回 null
     */
    SetupVO queryById(Long id);

    /**
     * 新增配置
     * <p>
     * 创建新的配置记录，默认状态为启用。
     * </p>
     *
     * @param dto 配置请求参数
     * @return 创建后的配置信息
     */
    SetupVO create(SetupDTO dto);

    /**
     * 修改配置
     * <p>
     * 根据 ID 更新配置的名称、API 密钥和模型地址。
     * </p>
     *
     * @param id  配置 ID
     * @param dto 配置请求参数
     * @return 更新后的配置信息
     */
    SetupVO update(Long id, SetupDTO dto);

    /**
     * 删除配置
     * <p>
     * 使用逻辑删除，不会真正删除数据库记录。
     * </p>
     *
     * @param id 配置 ID
     */
    void delete(Long id);

    /**
     * 切换配置状态
     * <p>
     * 将配置在启用和禁用之间切换。
     * </p>
     *
     * @param id  配置 ID
     * @param dto 状态切换请求参数
     * @return 更新后的状态值
     */
    String toggleStatus(Long id, StatusDTO dto);

    /**
     * 查询全部配置
     * <p>
     * 不分页，返回所有已启用的配置，供下拉选择等场景使用。
     * </p>
     *
     * @return 全部配置列表
     */
    List<SetupVO> queryAll();

    /**
     * 查询配置选项列表（下拉选择专用）
     * <p>
     * 仅返回 id 和 name，不包含 apiKey 等敏感字段，也不涉及 AES 解密，
     * 比 {@link #queryAll()} 更轻量安全，专用于 Agent 创建/编辑时的下拉选择。
     * </p>
     *
     * @return 配置选项列表，仅含 id 和 name
     */
    List<SetupOptionVO> queryOptions();
}