package com.mianshi.backend.model.dto.QuestionBankQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题库题目关联查询 DTO
 */
@Data
@Schema(description = "题库题目关联查询请求对象")
public class QuestionBankQuestionQueryDTO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "题库 id")
    private Long questionBankId;

    @Schema(description = "题目 id")
    private Long questionId;

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