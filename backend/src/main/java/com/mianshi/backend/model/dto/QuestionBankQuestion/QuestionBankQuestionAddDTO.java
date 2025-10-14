package com.mianshi.backend.model.dto.QuestionBankQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题库题目关联添加 DTO
 */
@Data
@Schema(description = "题库题目关联添加请求对象")
public class QuestionBankQuestionAddDTO {

    @Schema(description = "题库 id")
    @NotNull(message = "题库 id 不能为空")
    private Long questionBankId;

    @Schema(description = "题目 id")
    @NotNull(message = "题目 id 不能为空")
    private Long questionId;

    @Schema(description = "创建用户 id")
    private Long userId;
}