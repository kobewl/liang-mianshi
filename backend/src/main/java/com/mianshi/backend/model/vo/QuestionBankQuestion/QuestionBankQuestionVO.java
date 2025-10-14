package com.mianshi.backend.model.vo.QuestionBankQuestion;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库题目关联 VO
 */
@Data
@Schema(description = "题库题目关联响应对象")
public class QuestionBankQuestionVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "题库 id")
    private Long questionBankId;

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "创建用户 id")
    private Long userId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}