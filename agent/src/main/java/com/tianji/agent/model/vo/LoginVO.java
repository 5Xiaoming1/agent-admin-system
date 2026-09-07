package com.tianji.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应对象
 * <p>
 * 登录成功后返回给前端的视图对象，包含用户基本信息及 JWT 令牌，
 * 前端需将 token 存入本地存储并在后续请求的 Authorization 头中携带。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO {

    /** 用户唯一标识 */
    @Schema(description = "用户ID")
    private Long userId;

    /** 用户名 */
    @Schema(description = "用户名")
    private String username;

    /** JWT 令牌，前端需在后续请求中通过 Authorization 头携带 */
    @Schema(description = "JWT令牌")
    private String token;

    /** 管理员等级：2-超级管理员，1-管理员，0-普通用户 */
    @Schema(description = "管理员等级")
    private Integer admin;

    /** 头像URL */
    @Schema(description = "头像URL")
    private String avatar;

    /** 令牌类型，固定为 Bearer */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;
}