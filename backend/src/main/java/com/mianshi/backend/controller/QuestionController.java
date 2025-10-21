package com.mianshi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.model.dto.Question.QuestionAddDTO;
import com.mianshi.backend.model.dto.Question.QuestionUpdateDTO;
import com.mianshi.backend.model.dto.Question.QuestionQueryDTO;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.vo.Question.QuestionVO;
import com.mianshi.backend.service.QuestionService;
import com.mianshi.backend.model.vo.ApiResponse;
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
    public ApiResponse<Long> createQuestion(@RequestAttribute("userId") Long userId,
                                            @Valid @RequestBody QuestionAddDTO questionAddDTO) {
        questionAddDTO.setUserId(userId);
        return ApiResponse.success(questionService.addQuestion(questionAddDTO));
    }

    @Operation(summary = "更新题目", description = "根据 ID 更新题目信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateQuestion(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "题目ID") @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateDTO questionUpdateDTO) {
        questionUpdateDTO.setUserId(userId);
        return ApiResponse.success(questionService.updateQuestion(id, questionUpdateDTO));
    }

    @Operation(summary = "删除题目", description = "根据 ID 删除题目")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteQuestion(@Parameter(description = "题目ID") @PathVariable Long id) {
        return ApiResponse.success(questionService.removeById(id));
    }

    @Operation(summary = "获取题目详情", description = "根据 ID 获取题目详情")
    @GetMapping("/{id}")
    public ApiResponse<QuestionVO> getQuestion(@Parameter(description = "题目ID") @PathVariable Long id) {
        return ApiResponse.success(questionService.getQuestionById(id));
    }

    @Operation(summary = "分页查询题目", description = "分页查询题目列表")
    @GetMapping("/page")
    public ApiResponse<Page<QuestionVO>> pageQuestions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "标签列表") @RequestParam(required = false) String tags,
            @Parameter(description = "题目难度") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "创建用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "题库ID") @RequestParam(required = false) Long questionBankId) {
        QuestionQueryDTO queryDTO = new QuestionQueryDTO();
        queryDTO.setCurrent(current.intValue());
        queryDTO.setSize(size.intValue());
        queryDTO.setTitle(title);
        queryDTO.setTags(tags);
        queryDTO.setDifficulty(difficulty);
        queryDTO.setUserId(userId);
        queryDTO.setQuestionBankId(questionBankId);
        return ApiResponse.success(questionService.pageQuestions(queryDTO));
    }

    @Operation(summary = "分页从 ES 里查询数据", description = "分页从 ES 里查询数据")
    @GetMapping("/search/page/es")
    public ApiResponse<Page<Question>> pageQuestionsFromEs(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "标签列表") @RequestParam(required = false) String tags,
            @Parameter(description = "题目难度") @RequestParam(required = false) Integer difficulty,
            @Parameter(description = "创建用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "题库ID") @RequestParam(required = false) Long questionBankId) {
        QuestionQueryDTO queryDTO = new QuestionQueryDTO();
        queryDTO.setCurrent(current.intValue());
        queryDTO.setSize(size.intValue());
        queryDTO.setTitle(title);
        queryDTO.setTags(tags);
        queryDTO.setDifficulty(difficulty);
        queryDTO.setUserId(userId);
        queryDTO.setQuestionBankId(questionBankId);
        return ApiResponse.success(questionService.searchFromEs(queryDTO));
    }
}
