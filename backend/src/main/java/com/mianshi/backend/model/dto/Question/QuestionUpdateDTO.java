package com.mianshi.backend.model.dto.Question;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 题目更新 DTO
 */
@Data
public class QuestionUpdateDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    private String title;

    private String content;

    private String tags;

    private String answer;

    private Long userId;
}