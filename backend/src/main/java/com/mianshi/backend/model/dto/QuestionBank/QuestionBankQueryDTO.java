package com.mianshi.backend.model.dto.QuestionBank;

import lombok.Data;

/**
 * 题库查询 DTO
 */
@Data
public class QuestionBankQueryDTO {

    private Long id;

    private String title;

    private Long userId;

    private Integer current = 1;

    private Integer size = 10;

    private String sortField;

    private String sortOrder = "asc";
}