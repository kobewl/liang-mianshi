package com.mianshi.backend.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 用户添加 DTO
 */
@Data
public class UserAddDTO {

    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPassword;

    private String userName;


    private String userProfile;

    private String userRole;
}