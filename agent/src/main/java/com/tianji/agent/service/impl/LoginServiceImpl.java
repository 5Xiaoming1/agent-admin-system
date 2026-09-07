package com.tianji.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianji.agent.config.crypto.CryptoService;
import com.tianji.agent.config.jwt.JwtProperties;
import com.tianji.agent.config.jwt.JwtUtil;
import com.tianji.agent.config.redis.RedisUtils;
import com.tianji.agent.mapper.LoginLogMapper;
import com.tianji.agent.mapper.UserMapper;
import com.tianji.agent.model.dto.LoginDTO;
import com.tianji.agent.model.entity.LoginLog;
import com.tianji.agent.model.entity.User;
import com.tianji.agent.model.vo.LoginVO;
import com.tianji.agent.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录服务实现
 * <p>
 * 实现 {@link LoginService} 接口，完成用户登录的完整业务流程：
 * 验证码校验（从Redis获取） → 账号密码验证 → JWT 令牌生成 → 令牌存入Redis → 记录登录日志。
 * </p>
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    /** Redis 中存储 Token 的键前缀 */
    private static final String TOKEN_REDIS_PREFIX = "token:";

    /** 登录状态：成功 */
    private static final int LOGIN_STATUS_SUCCESS = 1;

    /** 登录状态：失败 */
    private static final int LOGIN_STATUS_FAIL = 0;

    /** JWT 配置属性，包含密钥、过期时间等 */
    private final JwtProperties jwtProperties;

    /** 用户持久层 Mapper，用于查询数据库中的用户信息 */
    private final UserMapper userMapper;

    /** Redis 工具类 */
    private final RedisUtils redisUtils;

    /** 加密解密服务组件 */
    private final CryptoService cryptoService;

    /** 登录日志持久层 Mapper，用于记录登录日志 */
    private final LoginLogMapper loginLogMapper;

    public LoginServiceImpl(JwtProperties jwtProperties,
                            UserMapper userMapper,
                            RedisUtils redisUtils,
                            CryptoService cryptoService,
                            LoginLogMapper loginLogMapper) {
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
        this.redisUtils = redisUtils;
        this.cryptoService = cryptoService;
        this.loginLogMapper = loginLogMapper;
    }

    /**
     * 用户登录
     * <p>
     * 按以下顺序进行校验：
     * <ol>
     *   <li>检查验证码是否过期（Redis 中无值则过期）</li>
     *   <li>比对用户提交的验证码与 Redis 中的验证码（大小写不敏感）</li>
     *   <li>根据用户名查询数据库，校验用户是否存在</li>
     *   <li>校验密码是否正确</li>
     *   <li>校验账号状态是否正常（未被禁用）</li>
     *   <li>生成 JWT 令牌，将用户 ID、用户名和管理员标识写入 claims</li>
     *   <li>将生成的 Token 存入 Redis，便于后续校验和登出</li>
     *   <li>记录登录日志到数据库</li>
     * </ol>
     * </p>
     *
     * @param loginDTO         登录请求参数
     * @param redisCaptchaCode Redis 中存储的验证码
     * @param ip               客户端IP地址
     * @return 登录响应，包含用户 ID、用户名和 JWT 令牌
     * @throws RuntimeException 校验失败时抛出对应异常
     */
    @Override
    public LoginVO login(LoginDTO loginDTO, String redisCaptchaCode, String ip) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String captcha = loginDTO.getCaptcha();

        log.info("用户登录请求: {}, IP: {}", username, ip);

        try {
            // 校验验证码是否过期
            if (redisCaptchaCode == null) {
                String failMsg = "验证码已过期，请刷新后重试";
                saveLoginLog(null, username, ip, LOGIN_STATUS_FAIL, failMsg);
                throw new RuntimeException(failMsg);
            }
            // 校验验证码是否匹配（大小写不敏感）
            if (!redisCaptchaCode.equalsIgnoreCase(captcha)) {
                String failMsg = "验证码错误";
                saveLoginLog(null, username, ip, LOGIN_STATUS_FAIL, failMsg);
                throw new RuntimeException(failMsg);
            }

            // 根据用户名查询数据库
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            User user = userMapper.selectOne(queryWrapper);

            // 用户不存在
            if (user == null) {
                String failMsg = "用户名或密码错误";
                saveLoginLog(null, username, ip, LOGIN_STATUS_FAIL, failMsg);
                throw new RuntimeException(failMsg);
            }

            // 密码不匹配
            String decryptedPassword = cryptoService.decrypt(user.getPassword());
            if (!decryptedPassword.equals(password)) {
                String failMsg = "用户名或密码错误";
                saveLoginLog(user.getId(), username, ip, LOGIN_STATUS_FAIL, failMsg);
                throw new RuntimeException(failMsg);
            }

            // 账号已被禁用
            if (user.getStatus() == 0) {
                String failMsg = "账号已被禁用，请联系管理员";
                saveLoginLog(user.getId(), username, ip, LOGIN_STATUS_FAIL, failMsg);
                throw new RuntimeException(failMsg);
            }

            // 构建 JWT claims，将用户信息写入令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            claims.put("admin", user.getAdmin());

            // 生成 JWT 令牌
            String token = JwtUtil.createJWT(
                    jwtProperties.getUserSecretKey(),
                    jwtProperties.getUserTtl(),
                    claims
            );

            // 将 Token 存入 Redis，key 为 token:{userId}，过期时间与 JWT 一致
            String redisTokenKey = TOKEN_REDIS_PREFIX + user.getId();
            long tokenTtlSeconds = jwtProperties.getUserTtl() / 1000;
            redisUtils.set(redisTokenKey, token, tokenTtlSeconds);
            log.info("用户 {} 登录成功, token已存入Redis, key: {}", username, redisTokenKey);

            // 记录登录成功日志
            saveLoginLog(user.getId(), username, ip, LOGIN_STATUS_SUCCESS, "登录成功");

            return LoginVO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .admin(user.getAdmin())
                    .avatar(user.getAvatar())
                    .token(token)
                    .tokenType("Bearer")
                    .build();
        } catch (RuntimeException e) {
            // 异常已在各校验点记录日志，此处直接向上抛出
            throw e;
        }
    }

    /**
     * 保存登录日志到数据库
     *
     * @param userId   用户ID，用户不存在时为 null
     * @param username 用户名
     * @param ip       客户端IP地址
     * @param status   登录状态：1-成功，0-失败
     * @param message  登录信息/失败原因
     */
    private void saveLoginLog(Long userId, String username, String ip, int status, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId != null ? userId : 0L);
        loginLog.setUsername(username);
        loginLog.setIp(ip);
        loginLog.setStatus(status);
        loginLog.setMessage(message);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(loginLog);
        log.info("登录日志已记录: username={}, status={}, message={}, ip={}", username, status, message, ip);
    }

}