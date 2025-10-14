package com.mianshi.backend.model.dto.QuestionBankQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题库题目关联更新 DTO
 */
@Data
@Schema(description = "题库题目关联更新请求对象")
public class QuestionBankQuestionUpdateDTO {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "题库 id")
    private Long questionBankId;

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "创建用户 id")
    private Long userId;
}