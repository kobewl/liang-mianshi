package com.mianshi.backend.model.dto.QuestionBank;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 题库添加 DTO
 */
@Data
@Schema(description = "题库添加请求对象")
public class QuestionBankAddDTO {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片")
    private String picture;

    @Schema(description = "创建用户 id")
    private Long userId;
}