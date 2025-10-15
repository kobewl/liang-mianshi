package com.mianshi.backend.model.dto.QuestionBankQuestion;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 题库题目关联添加 DTO
 */
@Data
public class QuestionBankQuestionAddDTO {

    @NotNull(message = "题库 id 不能为空")
    private Long questionBankId;

    @NotNull(message = "题目 id 不能为空")
    private Long questionId;

    private Long userId;
}