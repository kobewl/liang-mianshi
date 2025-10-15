package com.mianshi.backend.model.dto.QuestionBankQuestion;

import lombok.Data;

/**
 * 题库题目关联查询 DTO
 */
@Data
public class QuestionBankQuestionQueryDTO {

    private Long id;

    private Long questionBankId;

    private Long questionId;

    private Long userId;

    private Integer current = 1;

    private Integer size = 10;

    private String sortField;

    private String sortOrder = "asc";
}