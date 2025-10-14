package com.mianshi.backend.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库 VO
 */
@Data
@Schema(description = "题库响应对象")
public class QuestionBankVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片")
    private String picture;

    @Schema(description = "创建用户 id")
    private Long userId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
