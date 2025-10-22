package com.mianshi.backend.model.dto.Question;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量题库操作 DTO
 */
@Data
public class QuestionBatchRepoDTO {

    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> questionIds;

    @NotNull(message = "题库ID不能为空")
    private Long repoId;

    /**
     * 操作人 ID，由后端注入
     */
    private Long operatorId;
}

