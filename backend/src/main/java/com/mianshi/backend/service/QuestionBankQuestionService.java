package com.mianshi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mianshi.backend.model.entity.QuestionBankQuestion;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionAddDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryDTO;
import com.mianshi.backend.model.vo.QuestionBankQuestion.QuestionBankQuestionVO;

/**
 * 题库题目关联服务
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {
    
    Long addQuestionBankQuestion(QuestionBankQuestionAddDTO questionBankQuestionAddDTO);
    
    Boolean updateQuestionBankQuestion(Long id, QuestionBankQuestionUpdateDTO questionBankQuestionUpdateDTO);
    
    QuestionBankQuestionVO getQuestionBankQuestionById(Long id);
    
    Page<QuestionBankQuestionVO> pageQuestionBankQuestions(QuestionBankQuestionQueryDTO queryDTO);
}
