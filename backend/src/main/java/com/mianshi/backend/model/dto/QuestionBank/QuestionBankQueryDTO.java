package com.mianshi.backend.model.dto.QuestionBank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题库查询 DTO
 */
@Data
@Schema(description = "题库查询请求对象")
public class QuestionBankQueryDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "创建用户 id")
    private Long userId;

    @Schema(description = "当前页码")
    private Integer current = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "排序字段")
    private String sortField;

    @Schema(description = "排序方式")
    private String sortOrder = "asc";
}