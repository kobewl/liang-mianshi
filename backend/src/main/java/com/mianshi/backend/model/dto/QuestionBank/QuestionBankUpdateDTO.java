package com.mianshi.backend.model.dto.QuestionBank;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题库更新 DTO
 */
@Data
@Schema(description = "题库更新请求对象")
public class QuestionBankUpdateDTO {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片")
    private String picture;

    @Schema(description = "创建用户 id")
    private Long userId;
}