package com.mianshi.backend.model.dto.Question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 题目更新 DTO
 */
@Data
@Schema(description = "题目更新请求对象")
public class QuestionUpdateDTO {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "标题")
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