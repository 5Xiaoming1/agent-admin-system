package com.tianji.agent.config.common;

import com.tianji.agent.config.jwt.BaseContext;
import com.tianji.agent.model.entity.User;
import com.tianji.agent.mapper.UserMapper;
import com.tianji.agent.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志工具类
 * <p>
 * 提供便捷的操作日志记录方法，自动获取当前用户信息和请求IP地址。
 * 可在Service层或Controller层调用，用于记录用户的各类操作行为。
 * </p>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * OperationLogUtil.log("用户管理", "新增", "新增用户：张三");
 * OperationLogUtil.log("角色管理", "修改", "修改角色权限：管理员");
 * OperationLogUtil.log("系统设置", "删除", "删除配置项：XXX");
 * </pre>
 */
@Component
@Slf4j
public class OperationLogUtil {

    private static OperationLogService operationLogService;
    private static UserMapper userMapper;

    public OperationLogUtil(OperationLogService operationLogService, UserMapper userMapper) {
        OperationLogUtil.operationLogService = operationLogService;
        OperationLogUtil.userMapper = userMapper;
    }

    /**
     * 记录操作日志
     *
     * @param module      操作模块，如：用户管理、角色管理、系统设置等
     * @param action      操作类型，如：新增、修改、删除、查询、导出等
     * @param description 操作描述，如：新增用户张三、修改角色权限等
     */
    public static void log(String module, String action, String description) {
        try {
            Long userId = BaseContext.getCurrentId();
            String username = getUsername(userId);
            String ip = getClientIp();
            
            operationLogService.saveOperationLog(userId, username, module, action, description, ip);
        } catch (Exception e) {
            log.error("记录操作日志失败: module={}, action={}, description={}, error={}", 
                    module, action, description, e.getMessage(), e);
        }
    }

    /**
     * 获取用户名
     *
     * @param userId 用户ID
     * @return 用户名，获取失败时返回"未知用户"
     */
    private static String getUsername(Long userId) {
        try {
            if (userId == null) {
                return "未知用户";
            }
            User user = userMapper.selectById(userId);
            return user != null ? user.getUsername() : "未知用户";
        } catch (Exception e) {
            log.error("获取用户名失败: userId={}, error={}", userId, e.getMessage());
            return "未知用户";
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    private static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "127.0.0.1";
            }
            HttpServletRequest request = attributes.getRequest();
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip != null ? ip : "127.0.0.1";
        } catch (Exception e) {
            log.error("获取客户端IP失败: error={}", e.getMessage());
            return "127.0.0.1";
        }
    }
}