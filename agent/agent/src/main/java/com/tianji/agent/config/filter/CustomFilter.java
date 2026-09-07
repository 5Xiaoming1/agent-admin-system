package com.tianji.agent.config.filter;

import com.tianji.agent.config.jwt.BaseContext;
import com.tianji.agent.config.jwt.JwtProperties;
import com.tianji.agent.config.jwt.JwtUtil;
import com.tianji.agent.config.redis.RedisUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 基于 {@link OncePerRequestFilter} 实现全局 JWT 校验，
 * 优先从 Redis 中验证 Token 是否有效，再解析 JWT 获取用户信息。
 * </p>
 */
@Component
@Slf4j
public class CustomFilter extends OncePerRequestFilter {

    /** Redis 中存储 Token 的键前缀 */
    private static final String TOKEN_REDIS_PREFIX = "token:";

    private final JwtProperties jwtProperties;
    private final AntPathMatcher antPathMatcher;
    private final RedisUtils redisUtils;

    public CustomFilter(JwtProperties jwtProperties, RedisUtils redisUtils) {
        this.jwtProperties = jwtProperties;
        this.antPathMatcher = new AntPathMatcher();
        this.redisUtils = redisUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (isExclude(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            log.info("JWT校验: {}", token);

            // 先解析 JWT 获取 userId
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = claims.get("id", Long.class);
            log.info("当前用户id: {}", userId);

            // 优先从 Redis 校验 Token 是否有效
            String redisTokenKey = TOKEN_REDIS_PREFIX + userId;
            String redisToken = (String) redisUtils.get(redisTokenKey);
            if (redisToken == null) {
                log.warn("Token 在 Redis 中不存在或已过期, userId: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                return;
            }
            if (!redisToken.equals(token)) {
                log.warn("Token 不匹配, userId: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效，请重新登录\"}");
                return;
            }

            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("JWT校验失败: {}", ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    private boolean isExclude(String requestURI) {
        if (jwtProperties.getExcludePaths() == null) {
            return false;
        }
        for (String excludePath : jwtProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, requestURI)) {
                return true;
            }
        }
        return false;
    }

}