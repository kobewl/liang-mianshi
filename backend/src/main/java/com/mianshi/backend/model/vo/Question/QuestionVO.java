package com.mianshi.backend.model.vo.Question;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目 VO
 */
@Data
public class QuestionVO {

    private Long id;

    private String title;

    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    private String answer;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
