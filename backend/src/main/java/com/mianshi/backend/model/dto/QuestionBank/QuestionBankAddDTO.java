package com.mianshi.backend.model.dto.QuestionBank;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 题库添加 DTO
 */
@Data
public class QuestionBankAddDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private String picture;

    private Long userId;
}