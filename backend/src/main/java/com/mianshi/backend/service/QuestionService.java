package com.mianshi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.dto.Question.QuestionAddDTO;
import com.mianshi.backend.model.dto.Question.QuestionUpdateDTO;
import com.mianshi.backend.model.dto.Question.QuestionQueryDTO;
import com.mianshi.backend.model.vo.Question.QuestionVO;

/**
 * 题目服务
 */
public interface QuestionService extends IService<Question> {
    
    Long addQuestion(QuestionAddDTO questionAddDTO);
    
    Boolean updateQuestion(Long id, QuestionUpdateDTO questionUpdateDTO);
    
    QuestionVO getQuestionById(Long id);
    
    Page<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO);
}
