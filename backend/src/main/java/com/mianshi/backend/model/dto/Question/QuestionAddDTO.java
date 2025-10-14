package com.mianshi.backend.model.dto.Question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 题目添加 DTO
 */
@Data
@Schema(description = "题目添加请求对象")
public class QuestionAddDTO {

    @Schema(description = "标题")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "标签列表 (json 数组)")
    private String tags;

    @Schema(description = "推荐答案")
    private String answer;

    @Schema(description = "创建用户 id")
    private Long userId;
}