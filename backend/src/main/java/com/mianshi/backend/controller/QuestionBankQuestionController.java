package com.mianshi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionAddDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryDTO;
import com.mianshi.backend.model.vo.QuestionBankQuestion.QuestionBankQuestionVO;
import com.mianshi.backend.service.QuestionBankQuestionService;
import com.mianshi.backend.model.vo.ApiResponse;
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

    @Operation(summary = "创建题库题目关联", description = "创建新题库题目关联")
    @PostMapping
    public ApiResponse<Long> createQuestionBankQuestion(@Valid @RequestBody QuestionBankQuestionAddDTO questionBankQuestionAddDTO) {
        return ApiResponse.success(questionBankQuestionService.addQuestionBankQuestion(questionBankQuestionAddDTO));
    }

    @Operation(summary = "更新题库题目关联", description = "根据 ID 更新题库题目关联信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateQuestionBankQuestion(
            @Parameter(description = "题库题目关联ID") @PathVariable Long id,
            @Valid @RequestBody QuestionBankQuestionUpdateDTO questionBankQuestionUpdateDTO) {
        return ApiResponse.success(questionBankQuestionService.updateQuestionBankQuestion(id, questionBankQuestionUpdateDTO));
    }

    @Operation(summary = "删除题库题目关联", description = "根据 ID 删除题库题目关联")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteQuestionBankQuestion(@Parameter(description = "题库题目关联ID") @PathVariable Long id) {
        return ApiResponse.success(questionBankQuestionService.removeById(id));
    }

    @Operation(summary = "获取题库题目关联详情", description = "根据 ID 获取题库题目关联详情")
    @GetMapping("/{id}")
    public ApiResponse<QuestionBankQuestionVO> getQuestionBankQuestion(@Parameter(description = "题库题目关联ID") @PathVariable Long id) {
        return ApiResponse.success(questionBankQuestionService.getQuestionBankQuestionById(id));
    }

    @Operation(summary = "分页查询题库题目关联", description = "分页查询题库题目关联列表")
    @GetMapping("/page")
    public ApiResponse<Page<QuestionBankQuestionVO>> pageQuestionBankQuestions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "题库ID") @RequestParam(required = false) Long questionBankId,
            @Parameter(description = "题目ID") @RequestParam(required = false) Long questionId,
            @Parameter(description = "创建用户ID") @RequestParam(required = false) Long userId) {
        QuestionBankQuestionQueryDTO queryDTO = new QuestionBankQuestionQueryDTO();
        queryDTO.setCurrent(current.intValue());
        queryDTO.setSize(size.intValue());
        queryDTO.setQuestionBankId(questionBankId);
        queryDTO.setQuestionId(questionId);
        queryDTO.setUserId(userId);
        return ApiResponse.success(questionBankQuestionService.pageQuestionBankQuestions(queryDTO));
    }
}
