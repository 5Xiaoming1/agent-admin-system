package com.tianji.agent.controller;

import com.tianji.agent.config.common.Result;
import com.tianji.agent.config.redis.RedisUtils;
import com.tianji.agent.model.dto.LoginDTO;
import com.tianji.agent.model.vo.CaptchaVO;
import com.tianji.agent.model.vo.LoginVO;
import com.tianji.agent.service.LoginService;
import com.tianji.agent.config.captcha.CaptchaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 登录控制器
 * <p>
 * 处理用户登录请求，作为 Controller 层负责：
 * <ul>
 *   <li>生成验证码图片并存入 Redis</li>
 *   <li>接收并校验前端提交的登录表单数据</li>
 *   <li>从 Redis 中提取验证码传递给 Service 层</li>
 *   <li>获取客户端真实IP地址</li>
 *   <li>登录成功后移除 Redis 中的验证码（一次性使用）</li>
 *   <li>统一封装响应结果</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/login")
@Tag(name = "登录管理", description = "用户登录相关接口")
@Slf4j
@AllArgsConstructor
public class LoginController {

    /** Redis 中存储验证码的键前缀 */
    private static final String CAPTCHA_REDIS_PREFIX = "captcha:";

    /** 验证码过期时间：5分钟 */
    private static final long CAPTCHA_TTL = 300L;

    /** 未知IP时使用的默认值 */
    private static final String UNKNOWN_IP = "unknown";

    /** IPv6 localhost 环回地址 */
    private static final String IPV6_LOCALHOST = "0:0:0:0:0:0:0:1";

    /** IPv4 localhost 环回地址 */
    private static final String IPV4_LOCALHOST = "127.0.0.1";

    /** 登录服务 */
    private final LoginService loginService;

    /** Redis 工具类 */
    private final RedisUtils redisUtils;

    /**
     * 获取验证码
     * <p>
     * 生成新的验证码，将验证码文本以 {@value #CAPTCHA_REDIS_PREFIX}{uuid} 为键存入 Redis，
     * 同时返回 Base64 编码的验证码图片和验证码Key，前端登录时需回传验证码Key。
     * </p>
     *
     * @return 统一响应结果，data 包含验证码图片和验证码Key
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "生成验证码图片并存入Redis，返回Base64编码的图片和验证码Key")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        String captchaKey = UUID.randomUUID().toString();
        String redisKey = CAPTCHA_REDIS_PREFIX + captchaKey;
        redisUtils.set(redisKey, result.code(), CAPTCHA_TTL);
        log.info("验证码已生成, key: {}", redisKey);
        return Result.success(new CaptchaVO(result.base64Image(), captchaKey, null));
    }

    /**
     * 获取验证码（测试用）
     * <p>
     * 生成新的验证码，将验证码文本以 {@value #CAPTCHA_REDIS_PREFIX}{uuid} 为键存入 Redis，
     * 同时返回 Base64 编码的验证码图片、验证码Key<strong>和验证码明文</strong>，仅用于后端调试测试，
     * 前端生产环境请使用 {@code GET /api/login/captcha}。
     * </p>
     *
     * @return 统一响应结果，data 包含验证码图片、验证码Key和验证码明文
     */
    @GetMapping("/captcha/test")
    @Operation(summary = "获取验证码（测试用）", description = "生成验证码图片并存入Redis，返回Base64编码的图片、验证码Key和验证码明文，仅用于测试")
    public Result<CaptchaVO> getCaptchaTest() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        String captchaKey = UUID.randomUUID().toString();
        String redisKey = CAPTCHA_REDIS_PREFIX + captchaKey;
        redisUtils.set(redisKey, result.code(), CAPTCHA_TTL);
        log.info("测试验证码已生成, key: {}", redisKey);
        return Result.success(new CaptchaVO(result.base64Image(), captchaKey, result.code()));
    }

    /**
     * 用户登录
     * <p>
     * 接收包含用户名、密码、验证码和验证码Key的登录请求，从 Redis 中获取之前生成的验证码
     * 进行校验，校验通过后生成 JWT 令牌返回。验证码使用后立即从 Redis 中移除，
     * 防止重放攻击。同时记录登录日志（成功/失败）到数据库。
     * </p>
     *
     * @param loginDTO 登录请求参数，包含用户名、密码、验证码和验证码Key
     * @param request  HTTP请求对象，用于获取客户端IP地址
     * @return 统一响应结果，成功时 data 包含用户信息和 JWT 令牌
     */
    @PostMapping
    @Operation(summary = "用户登录", description = "使用用户名、密码和验证码登录，成功后返回JWT令牌")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        // 获取客户端真实IP地址，优先从代理头获取，其次从请求的远程地址获取
        String clientIp = getClientIp(request);
        log.info("登录请求: {}, IP: {}", loginDTO.getUsername(), clientIp);

        try {
            // 从 Redis 中取出验证码
            String redisKey = CAPTCHA_REDIS_PREFIX + loginDTO.getCaptchaKey();
            String redisCaptcha = (String) redisUtils.get(redisKey);
            // 将客户端IP传入Service层，用于记录登录日志
            LoginVO loginVO = loginService.login(loginDTO, redisCaptcha, clientIp);
            // 验证码一次性使用，校验通过后移除
            redisUtils.delete(redisKey);
            return Result.success("登录成功", loginVO);
        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 按优先级依次尝试从以下来源获取IP：
     * <ol>
     *   <li>X-Forwarded-For 请求头（取第一个IP，适用于经过代理/负载均衡的场景）</li>
     *   <li>X-Real-IP 请求头（Nginx 等代理设置的客户端真实IP）</li>
     *   <li>request.getRemoteAddr()（直连场景下的客户端IP）</li>
     * </ol>
     * 如果获取到的是 IPv6 localhost（::1），则统一转换为 127.0.0.1。
     * </p>
     *
     * @param request HTTP请求对象
     * @return 客户端真实IP地址，获取失败时返回 "unknown"
     */
    private String getClientIp(HttpServletRequest request) {
        // 优先从 X-Forwarded-For 中获取（取第一个IP，防止伪造）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            // 多次代理后会形成逗号分隔的IP列表，取第一个
            int commaIndex = ip.indexOf(',');
            if (commaIndex > 0) {
                ip = ip.substring(0, commaIndex).trim();
            }
            return normalizeIp(ip);
        }
        // 其次从 X-Real-IP 中获取
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return normalizeIp(ip);
        }
        // 最后使用直连IP
        ip = request.getRemoteAddr();
        if (ip != null && !ip.isEmpty()) {
            return normalizeIp(ip);
        }
        return UNKNOWN_IP;
    }

    /**
     * 规范化IP地址
     * <p>
     * 将 IPv6 localhost（0:0:0:0:0:0:0:1）转换为 IPv4 localhost（127.0.0.1），
     * 使日志中的IP更直观易读。
     * </p>
     *
     * @param ip 原始IP地址
     * @return 规范化后的IP地址
     */
    private String normalizeIp(String ip) {
        if (IPV6_LOCALHOST.equals(ip)) {
            return IPV4_LOCALHOST;
        }
        return ip;
    }

}