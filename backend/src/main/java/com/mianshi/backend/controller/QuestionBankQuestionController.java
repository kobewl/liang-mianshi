package com.mianshi.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.dto.QuestionBankQuestionDTO;
import com.mianshi.backend.entity.QuestionBankQuestion;
import com.mianshi.backend.service.QuestionBankQuestionService;
import com.mianshi.backend.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 题库题目关联控制器
 */
@Tag(name = "题库题目关联管理", description = "题库题目关联相关接口")
@RestController
@RequestMapping("/question-bank-question")
@RequiredArgsConstructor
public class QuestionBankQuestionController {

    private final QuestionBankQuestionService questionBankQuestionService;

    @Operation(summary = "添加题目到题库", description = "将题目添加到指定题库")
    @PostMapping
    public ApiResponse<Long> addQuestionToBank(@Valid @RequestBody QuestionBankQuestionDTO dto) {
        QuestionBankQuestion relation = BeanUtil.copyProperties(dto, QuestionBankQuestion.class);
        questionBankQuestionService.save(relation);
        return ApiResponse.success(relation.getId());
    }

    @Operation(summary = "从题库移除题目", description = "从题库中移除指定题目")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> removeQuestionFromBank(@Parameter(description = "关联ID") @PathVariable Long id) {
        return ApiResponse.success(questionBankQuestionService.removeById(id));
    }

    @Operation(summary = "查询题库下的题目", description = "根据题库 ID 查询题目列表")
    @GetMapping("/by-bank/{bankId}")
    public ApiResponse<Page<QuestionBankQuestion>> getQuestionsByBankId(
            @Parameter(description = "题库ID") @PathVariable Long bankId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionBankId", bankId);
        Page<QuestionBankQuestion> page = questionBankQuestionService.page(new Page<>(current, size), queryWrapper);
        return ApiResponse.success(page);
    }
}
