package com.tianji.agent.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应对象
 * <p>
 * 包含验证码图片、验证码Key（用于登录时从Redis定位验证码），
 * 测试接口额外返回验证码明文。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码响应对象")
public class CaptchaVO {

    /** Base64 编码的验证码图片，格式为 {@code data:image/png;base64,...} */
    @JsonProperty("captchaImage")
    @Schema(description = "Base64编码的验证码图片")
    private String image;

    /** 验证码Key，用于登录时从Redis定位验证码 */
    @JsonProperty("captchaKey")
    @Schema(description = "验证码Key，登录时需回传")
    private String captchaKey;

    /** 验证码明文，仅用于测试 */
    @JsonProperty("captchaCode")
    @Schema(description = "验证码明文（仅测试用）")
    private String code;

}