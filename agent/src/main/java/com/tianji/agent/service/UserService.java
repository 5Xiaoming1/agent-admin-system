package com.tianji.agent.service;

import com.tianji.agent.model.dto.ChangePasswordDTO;
import com.tianji.agent.model.dto.ProfileUpdateDTO;
import com.tianji.agent.model.dto.UserDTO;
import com.tianji.agent.model.dto.UserStatusDTO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.model.vo.UserVO;

/**
 * 用户管理服务接口
 * <p>
 * 定义用户管理的核心业务操作，包括分页查询、按ID查询、新增、修改、删除和状态切换。
 * </p>
 *
 * <p><b>业务说明：</b></p>
 * <ul>
 *   <li>用户名（username）用于登录账号，全局唯一</li>
 *   <li>密码（password）在新增时默认值为 "123456"</li>
 *   <li>管理员等级（admin）：2-超级管理员，1-管理员，0-普通用户</li>
 *   <li>状态（status）：1-正常，0-禁用</li>
 *   <li>删除操作为逻辑删除，而非物理删除</li>
 * </ul>
 */
public interface UserService {

    /**
     * 分页查询用户列表
     *
     * @param page     页码，从1开始
     * @param pageSize 每页条数
     * @param keyword  关键字（用户名模糊搜索）
     * @param status   状态筛选（1-正常，0-禁用），null 表示不筛选
     * @return 分页用户数据
     */
    PageVO<UserVO> queryPage(Integer page, Integer pageSize, String keyword, String status);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户详情
     * @throws RuntimeException 用户不存在时抛出
     */
    UserVO queryById(Long id);

    /**
     * 新增用户
     * <p>
     * 校验用户名唯一性后创建用户，默认密码为 "123456"，默认状态为启用。
     * </p>
     *
     * @param dto 用户创建请求参数
     * @return 创建成功的用户信息
     * @throws RuntimeException 用户名已存在时抛出
     */
    UserVO create(UserDTO dto);

    /**
     * 修改用户
     * <p>
     * 支持修改用户名、密码、管理员等级。
     * 密码为空时保留原密码不变。
     * </p>
     *
     * @param id  用户ID
     * @param dto 用户修改请求参数
     * @return 修改后的用户信息
     * @throws RuntimeException 用户不存在或用户名已存在时抛出
     */
    UserVO update(Long id, UserDTO dto);

    /**
     * 删除用户
     * <p>
     * 逻辑删除用户。
     * </p>
     *
     * @param id 用户ID
     * @throws RuntimeException 用户不存在时抛出
     */
    void delete(Long id);

    /**
     * 切换用户状态
     *
     * @param id  用户ID
     * @param dto 状态切换请求参数（含目标状态值）
     * @throws RuntimeException 用户不存在时抛出
     */
    void toggleStatus(Long id, UserStatusDTO dto);

    /**
     * 获取当前登录用户信息
     * <p>
     * 从 BaseContext 中获取当前用户ID，查询用户信息并返回。
     * 返回结果包含密码明文，用于前端个人信息页面展示。
     * </p>
     *
     * @return 当前用户信息（含密码明文）
     * @throws RuntimeException 用户不存在时抛出
     */
    UserVO getProfile();

    /**
     * 当前用户修改个人信息
     * <p>
     * 仅允许修改用户名，校验用户名唯一性后更新。
     * 返回结果不包含密码字段。
     * </p>
     *
     * @param dto 个人信息修改请求参数（仅含用户名）
     * @return 修改后的用户信息（不含密码）
     * @throws RuntimeException 用户不存在或用户名已存在时抛出
     */
    UserVO updateProfile(ProfileUpdateDTO dto);

    /**
     * 当前用户修改密码
     * <p>
     * 校验原密码正确后，更新为新密码。
     * </p>
     *
     * @param dto 修改密码请求参数（含原密码和新密码）
     * @throws RuntimeException 用户不存在或原密码错误时抛出
     */
    void changePassword(ChangePasswordDTO dto);
}