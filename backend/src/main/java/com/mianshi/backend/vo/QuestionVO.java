package com.mianshi.backend.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目 VO
 */
@Data
@Schema(description = "题目响应对象")
public class QuestionVO {

    @Schema(description = "id")
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

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
