package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 * <p>
 * 前端登录时提交的表单数据，包含用户名、密码和验证码，
 * 所有字段均使用 {@link NotBlank} 进行非空校验。
 * </p>
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO {

    /** 用户名，不能为空 */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 密码，不能为空 */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    /** 验证码，不能为空，需与 Redis 中存储的验证码比对 */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", example = "A3kM")
    private String captcha;

    /** 验证码Key，不能为空，用于从 Redis 中定位验证码 */
    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String captchaKey;

}