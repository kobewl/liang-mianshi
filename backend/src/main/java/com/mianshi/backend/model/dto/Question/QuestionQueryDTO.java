package com.mianshi.backend.model.dto.Question;

import lombok.Data;

/**
 * 题目查询 DTO
 */
@Data
public class QuestionQueryDTO {

    private Long id;

    private String title;

    private String tags;

    private Long userId;

    private Integer current = 1;

    private Integer size = 10;

    private String sortField;

    private String sortOrder = "asc";
}