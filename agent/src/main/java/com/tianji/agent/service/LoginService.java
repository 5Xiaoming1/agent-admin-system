package com.tianji.agent.service;

import com.tianji.agent.model.dto.LoginDTO;
import com.tianji.agent.model.vo.LoginVO;

/**
 * 登录服务接口
 * <p>
 * 定义用户登录的核心业务逻辑，包括验证码校验（从Redis获取）、账号密码验证和 JWT 令牌生成。
 * 由 {@link com.tianji.agent.service.impl.LoginServiceImpl} 提供具体实现。
 * </p>
 */
public interface LoginService {

    /**
     * 用户登录
     * <p>
     * 校验用户提交的验证码是否与 Redis 中存储的一致，
     * 验证用户名和密码，成功后生成 JWT 令牌并存入 Redis 返回。
     * 同时记录登录日志（成功/失败）到数据库。
     * </p>
     *
     * @param loginDTO         登录请求参数，包含用户名、密码、验证码和验证码Key
     * @param redisCaptchaCode Redis 中存储的验证码文本，由 Controller 层传入
     * @param ip               客户端IP地址，用于登录日志记录
     * @return 登录成功后的响应对象，包含用户信息和 JWT 令牌
     * @throws RuntimeException 验证码过期、验证码错误、用户名或密码错误时抛出
     */
    LoginVO login(LoginDTO loginDTO, String redisCaptchaCode, String ip);

}