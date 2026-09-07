package com.tianji.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "当前用户修改个人信息请求对象")
public class ProfileUpdateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需在2~20字符之间")
    @Schema(description = "用户名，2~20字符", example = "新用户名")
    private String username;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}