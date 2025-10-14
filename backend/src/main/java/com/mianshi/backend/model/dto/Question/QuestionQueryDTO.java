package com.mianshi.backend.model.dto.Question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目查询 DTO
 */
@Data
@Schema(description = "题目查询请求对象")
public class QuestionQueryDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "标签列表 (json 数组)")
    private String tags;

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