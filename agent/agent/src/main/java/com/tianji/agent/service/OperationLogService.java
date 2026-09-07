package com.tianji.agent.service;

import com.tianji.agent.model.vo.OperationLogVO;
import com.tianji.agent.model.vo.PageVO;

import java.util.List;

/**
 * 操作日志服务接口
 * <p>
 * 定义操作日志的查询和删除操作，删除操作为逻辑删除。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>操作日志由系统在用户执行操作时自动记录</li>
 *   <li>操作模块（module）：记录操作所属的功能模块</li>
 *   <li>操作类型（action）：记录具体的操作类型，如新增、修改、删除等</li>
 *   <li>操作描述（description）：记录操作的详细描述信息</li>
 *   <li>IP地址（ip）：记录操作时的客户端IP地址</li>
 *   <li>删除操作为逻辑删除，将 is_deleted 字段设为 1</li>
 * </ul>
 */
public interface OperationLogService {

    /**
     * 分页查询操作日志
     *
     * @param page     页码，从1开始
     * @param pageSize 每页条数
     * @param keyword  关键字（用户名模糊搜索）
     * @param module   操作模块筛选，null 表示不筛选
     * @return 分页操作日志数据
     */
    PageVO<OperationLogVO> queryPage(Integer page, Integer pageSize, String keyword, String module);

    /**
     * 保存操作日志
     *
     * @param userId      用户ID
     * @param username    用户名
     * @param module      操作模块
     * @param action      操作类型
     * @param description 操作描述
     * @param ip          IP地址
     */
    void saveOperationLog(Long userId, String username, String module, String action, String description, String ip);

    /**
     * 删除操作日志
     * <p>
     * 逻辑删除，将 is_deleted 字段设为 1。传递几个 ID 就删除几个。
     * </p>
     *
     * @param ids 操作日志 ID 列表（传 1 个则删 1 个，传多个则批量删除）
     */
    void deleteByIds(List<Long> ids);

    /**
     * 删除全部操作日志
     * <p>
     * 逻辑删除所有操作日志记录，将 is_deleted 字段设为 1。
     * </p>
     */
    void deleteAll();
}