package com.mianshi.backend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户更新 DTO
 */
@Data
@Schema(description = "用户更新请求对象")
public class UserUpdateDTO {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "账号")
    private String userAccount;

    @Schema(description = "密码")
    private String userPassword;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户简介")
    private String userProfile;

    @Schema(description = "用户角色")
    private String userRole;
}