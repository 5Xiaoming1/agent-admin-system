package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.mapper.OperationLogMapper;
import com.tianji.agent.model.entity.OperationLog;
import com.tianji.agent.model.vo.OperationLogVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 * <p>
 * 实现 {@link OperationLogService} 接口，提供操作日志的分页查询和逻辑删除功能。
 * 操作日志由系统在用户执行操作时自动记录，删除操作为逻辑删除。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>操作日志由系统在用户执行操作时自动记录</li>
 *   <li>操作模块（module）：记录操作所属的功能模块，如用户管理、角色管理等</li>
 *   <li>操作类型（action）：记录具体的操作类型，如新增、修改、删除等</li>
 *   <li>操作描述（description）：记录操作的详细描述信息</li>
 *   <li>IP地址（ip）：记录操作时的客户端IP地址</li>
 *   <li>查询结果按操作时间倒序排列</li>
 *   <li>删除操作为逻辑删除，通过 MyBatis-Plus @TableLogic 自动实现</li>
 * </ul>
 */
@Service
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {

    /** 操作日志持久层 Mapper */
    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public PageVO<OperationLogVO> queryPage(Integer page, Integer pageSize, String keyword, String module) {
        log.info("分页查询操作日志: page={}, pageSize={}, keyword={}, module={}", page, pageSize, keyword, module);

        Page<OperationLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<OperationLog> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(OperationLog::getUsername, keyword);
        }
        if (module != null && !module.isEmpty()) {
            queryWrapper.eq(OperationLog::getModule, module);
        }
        queryWrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> entityPage = operationLogMapper.selectPage(pageParam, queryWrapper);
        List<OperationLogVO> voList = entityPage.getRecords().stream()
                .map(OperationLogVO::fromEntity)
                .collect(Collectors.toList());

        Page<OperationLogVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public void saveOperationLog(Long userId, String username, String module, String action, String description, String ip) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setUsername(username);
        operationLog.setModule(module);
        operationLog.setAction(action);
        operationLog.setDescription(description);
        operationLog.setIp(ip);
        operationLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(operationLog);
        log.info("操作日志已记录: userId={}, username={}, module={}, action={}, description={}, ip={}", 
                userId, username, module, action, description, ip);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("逻辑删除操作日志: ids={}", ids);
        operationLogMapper.deleteBatchIds(ids);
        log.info("操作日志已删除: 数量={}", ids.size());
    }

    @Override
    public void deleteAll() {
        log.info("逻辑删除全部操作日志");
        operationLogMapper.delete(new LambdaQueryWrapper<>());
        log.info("全部操作日志已删除");
    }
}