package com.mianshi.backend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询 DTO
 */
@Data
@Schema(description = "用户查询请求对象")
public class UserQueryDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "账号")
    private String userAccount;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户角色")
    private String userRole;

    @Schema(description = "当前页码")
    private Integer current = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder = "asc";
}