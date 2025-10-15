package com.mianshi.backend.model.vo.user;

import lombok.Data;

/**
 * 用户登录响应 VO
 */
@Data
public class UserLoginVO {

    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private String token;
}