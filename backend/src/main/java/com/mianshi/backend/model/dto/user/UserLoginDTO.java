package com.mianshi.backend.model.dto.user;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 用户登录 DTO
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPassword;
}