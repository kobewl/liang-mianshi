package com.mianshi.backend.model.vo.QuestionBankQuestion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库题目关联 VO
 */
@Data
public class QuestionBankQuestionVO {

    private Long id;

    private Long questionBankId;

    private Long questionId;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}