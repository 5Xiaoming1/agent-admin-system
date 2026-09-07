package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户管理请求对象")
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "密码（新增时必填，修改时可选）", example = "123456")
    private String password;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "管理员等级：2-超级管理员，1-管理员，0-普通用户", example = "0")
    private Integer admin;
}