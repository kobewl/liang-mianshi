package com.mianshi.backend.model.vo.QuestionBank;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库 VO
 */
@Data
public class QuestionBankVO {

    private Long id;

    private String title;

    private String description;

    private String picture;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}