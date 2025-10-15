package com.mianshi.backend.model.dto.QuestionBank;


import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 题库更新 DTO
 */
@Data
public class QuestionBankUpdateDTO {

    private Long id;

    private String title;

    private String description;

    private String picture;

    private Long userId;
}