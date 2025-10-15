package com.mianshi.backend.model.dto.user;

import lombok.Data;

/**
 * 用户查询 DTO
 */
@Data
public class UserQueryDTO {

    private Long id;

    private String userAccount;

    private String userName;

    private String userRole;

    private Integer current = 1;

    private Integer size = 10;

    private String sortField;

    private String sortOrder = "asc";
}