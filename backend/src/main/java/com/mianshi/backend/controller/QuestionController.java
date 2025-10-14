package com.mianshi.backend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.dto.QuestionDTO;
import com.mianshi.backend.entity.Question;
import com.mianshi.backend.service.QuestionService;
import com.mianshi.backend.vo.ApiResponse;
import com.mianshi.backend.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 题目控制器
 */
@Tag(name = "题目管理", description = "题目相关接口")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "创建题目", description = "创建新题目")
    @PostMapping
    public ApiResponse<Long> createQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        Question question = BeanUtil.copyProperties(questionDTO, Question.class);
        questionService.save(question);
        return ApiResponse.success(question.getId());
    }

    @Operation(summary = "更新题目", description = "根据 ID 更新题目信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateQuestion(
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Valid @RequestBody QuestionDTO questionDTO) {
        Question question = BeanUtil.copyProperties(questionDTO, Question.class);
        question.setId(id);
        return ApiResponse.success(questionService.updateById(question));
    }

    @Operation(summary = "删除题目", description = "根据 ID 删除题目")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteQuestion(@Parameter(description = "题目ID") @PathVariable Long id) {
        return ApiResponse.success(questionService.removeById(id));
    }

    @Operation(summary = "获取题目详情", description = "根据 ID 获取题目详情")
    @GetMapping("/{id}")
    public ApiResponse<QuestionVO> getQuestion(@Parameter(description = "题目ID") @PathVariable Long id) {
        Question question = questionService.getById(id);
        QuestionVO questionVO = BeanUtil.copyProperties(question, QuestionVO.class);
        return ApiResponse.success(questionVO);
    }

    @Operation(summary = "分页查询题目", description = "分页查询题目列表")
    @GetMapping("/page")
    public ApiResponse<Page<QuestionVO>> pageQuestions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size) {
        Page<Question> page = questionService.page(new Page<>(current, size), new QueryWrapper<>());
        Page<QuestionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(BeanUtil.copyToList(page.getRecords(), QuestionVO.class));
        return ApiResponse.success(voPage);
    }
}
