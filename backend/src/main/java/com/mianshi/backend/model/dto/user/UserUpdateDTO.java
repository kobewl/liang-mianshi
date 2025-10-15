package com.mianshi.backend.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 用户更新 DTO
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;
}