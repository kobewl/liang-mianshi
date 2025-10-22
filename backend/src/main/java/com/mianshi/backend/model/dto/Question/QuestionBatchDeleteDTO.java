package com.mianshi.backend.model.dto.Question;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除题目 DTO
 */
@Data
public class QuestionBatchDeleteDTO {

    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> questionIds;

    /**
     * 操作人 ID，由后端注入
     */
    private Long operatorId;
}

