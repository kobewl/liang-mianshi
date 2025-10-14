package com.mianshi.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户 DTO
 */
@Data
@Schema(description = "用户请求对象")
public class UserDTO {

    @Schema(description = "账号")
    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String userPassword;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户简介")
    private String userProfile;
}
