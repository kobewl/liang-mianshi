package com.mianshi.backend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录 DTO
 */
@Data
@Schema(description = "用户登录请求对象")
public class UserLoginDTO {

    @Schema(description = "账号")
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String userPassword;
}