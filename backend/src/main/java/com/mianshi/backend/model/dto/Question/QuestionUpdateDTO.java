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

    /**
     * 题目难度（1-基础 2-简单 3-中等 4-困难）
     */
    private Integer difficulty;

    private String answer;

    private Long userId;
}
