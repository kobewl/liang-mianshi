package com.mianshi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankAddDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankQueryDTO;
import com.mianshi.backend.model.vo.QuestionBank.QuestionBankVO;
import com.mianshi.backend.service.QuestionBankService;
import com.mianshi.backend.model.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 题库控制器
 */
@Tag(name = "题库管理", description = "题库相关接口")
@RestController
@RequestMapping("/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @Operation(summary = "创建题库", description = "创建新题库")
    @PostMapping
    public ApiResponse<Long> createQuestionBank(@RequestAttribute("userId") Long userId,
                                                @Valid @RequestBody QuestionBankAddDTO questionBankAddDTO) {
        questionBankAddDTO.setUserId(userId);
        return ApiResponse.success(questionBankService.addQuestionBank(questionBankAddDTO));
    }

    @Operation(summary = "更新题库", description = "根据 ID 更新题库信息")
    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateQuestionBank(
            @Parameter(description = "题库ID") @PathVariable Long id,
            @Valid @RequestBody QuestionBankUpdateDTO questionBankUpdateDTO) {
        return ApiResponse.success(questionBankService.updateQuestionBank(id, questionBankUpdateDTO));
    }

    @Operation(summary = "删除题库", description = "根据 ID 删除题库")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteQuestionBank(@Parameter(description = "题库ID") @PathVariable Long id) {
        return ApiResponse.success(questionBankService.removeById(id));
    }

    @Operation(summary = "获取题库详情", description = "根据 ID 获取题库详情")
    @GetMapping("/{id}")
    public ApiResponse<QuestionBankVO> getQuestionBank(@Parameter(description = "题库ID") @PathVariable Long id) {
        return ApiResponse.success(questionBankService.getQuestionBankById(id));
    }

    @Operation(summary = "分页查询题库", description = "分页查询题库列表")
    @GetMapping("/page")
    public ApiResponse<Page<QuestionBankVO>> pageQuestionBanks(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "创建用户ID") @RequestParam(required = false) Long userId) {
        QuestionBankQueryDTO queryDTO = new QuestionBankQueryDTO();
        queryDTO.setCurrent(current.intValue());
        queryDTO.setSize(size.intValue());
        queryDTO.setTitle(title);
        queryDTO.setUserId(userId);
        return ApiResponse.success(questionBankService.pageQuestionBanks(queryDTO));
    }
}
