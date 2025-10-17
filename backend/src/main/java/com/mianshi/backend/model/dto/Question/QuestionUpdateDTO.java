package com.mianshi.backend.model.dto.Question;

import lombok.Data;

import java.util.List;

/**
 * 题目更新 DTO
 */
@Data
public class QuestionUpdateDTO {

    private String title;

    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private String answer;

    private Long userId;
}
