package com.tianji.agent.service;

import com.tianji.agent.model.vo.LoginLogVO;
import com.tianji.agent.model.vo.PageVO;

import java.util.List;

/**
 * 登录日志服务接口
 * <p>
 * 定义登录日志的查询和删除操作，删除操作为逻辑删除。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>登录日志由系统在用户登录时自动记录</li>
 *   <li>登录状态（status）：1-成功，0-失败</li>
 *   <li>登录信息（message）：成功时记录登录成功信息，失败时记录失败原因</li>
 *   <li>IP地址（ip）：记录用户登录时的客户端IP地址</li>
 *   <li>删除操作为逻辑删除，将 is_deleted 字段设为 1</li>
 * </ul>
 */
public interface LoginLogService {

    /**
     * 分页查询登录日志
     *
     * @param page     页码，从1开始
     * @param pageSize 每页条数
     * @param keyword  关键字（用户名模糊搜索）
     * @param status   状态筛选（1-成功，0-失败），null 表示不筛选
     * @return 分页登录日志数据
     */
    PageVO<LoginLogVO> queryPage(Integer page, Integer pageSize, String keyword, String status);

    /**
     * 删除登录日志
     * <p>
     * 逻辑删除，将 is_deleted 字段设为 1。传递几个 ID 就删除几个。
     * </p>
     *
     * @param ids 登录日志 ID 列表（传 1 个则删 1 个，传多个则批量删除）
     */
    void deleteByIds(List<Long> ids);

    /**
     * 删除全部登录日志
     * <p>
     * 逻辑删除所有登录日志记录，将 is_deleted 字段设为 1。
     * </p>
     */
    void deleteAll();
}