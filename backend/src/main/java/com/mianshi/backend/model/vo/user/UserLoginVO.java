package com.mianshi.backend.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录响应 VO
 */
@Data
@Schema(description = "用户登录响应对象")
public class UserLoginVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "账号")
    private String userAccount;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户简介")
    private String userProfile;

    @Schema(description = "用户角色")
    private String userRole;

    @Schema(description = "JWT Token")
    private String token;
}