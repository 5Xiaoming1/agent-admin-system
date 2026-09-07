package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.agent.mapper.LoginLogMapper;
import com.tianji.agent.model.entity.LoginLog;
import com.tianji.agent.model.vo.LoginLogVO;
import com.tianji.agent.model.vo.PageVO;
import com.tianji.agent.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志服务实现
 * <p>
 * 实现 {@link LoginLogService} 接口，提供登录日志的分页查询和逻辑删除功能。
 * 登录日志由系统在用户登录时自动记录，删除操作为逻辑删除。
 * </p>
 *
 * <p><b>数据说明：</b></p>
 * <ul>
 *   <li>登录日志由系统在用户登录时自动记录</li>
 *   <li>登录状态（status）：1-成功，0-失败</li>
 *   <li>登录信息（message）：成功时记录登录成功信息，失败时记录失败原因</li>
 *   <li>IP地址（ip）：记录用户登录时的客户端IP地址</li>
 *   <li>查询结果按登录时间倒序排列</li>
 *   <li>删除操作为逻辑删除，通过 MyBatis-Plus @TableLogic 自动实现</li>
 * </ul>
 */
@Service
@Slf4j
public class LoginLogServiceImpl implements LoginLogService {

    /** 登录日志持久层 Mapper */
    private final LoginLogMapper loginLogMapper;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
    }

    @Override
    public PageVO<LoginLogVO> queryPage(Integer page, Integer pageSize, String keyword, String status) {
        log.info("分页查询登录日志: page={}, pageSize={}, keyword={}, status={}", page, pageSize, keyword, status);

        Page<LoginLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<LoginLog> queryWrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(LoginLog::getUsername, keyword);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(LoginLog::getStatus, "success".equals(status) ? 1 : 0);
        }
        queryWrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> entityPage = loginLogMapper.selectPage(pageParam, queryWrapper);
        List<LoginLogVO> voList = entityPage.getRecords().stream()
                .map(LoginLogVO::fromEntity)
                .collect(Collectors.toList());

        Page<LoginLogVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return PageVO.fromPage(voPage);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        log.info("逻辑删除登录日志: ids={}", ids);
        loginLogMapper.deleteBatchIds(ids);
        log.info("登录日志已删除: 数量={}", ids.size());
    }

    @Override
    public void deleteAll() {
        log.info("逻辑删除全部登录日志");
        loginLogMapper.delete(new LambdaQueryWrapper<>());
        log.info("全部登录日志已删除");
    }
}