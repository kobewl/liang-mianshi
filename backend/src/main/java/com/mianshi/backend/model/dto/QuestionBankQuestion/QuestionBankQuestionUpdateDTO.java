package com.mianshi.backend.model.dto.QuestionBankQuestion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 题库题目关联更新 DTO
 */
@Data
public class QuestionBankQuestionUpdateDTO {

    @NotNull(message = "id不能为空")
    private Long id;

    private Long questionBankId;

    private Long questionId;

    private Long userId;
}