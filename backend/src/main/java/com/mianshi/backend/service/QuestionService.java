package com.mianshi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.dto.Question.QuestionAddDTO;
import com.mianshi.backend.model.dto.Question.QuestionUpdateDTO;
import com.mianshi.backend.model.dto.Question.QuestionQueryDTO;
import com.mianshi.backend.model.vo.Question.QuestionVO;

import java.util.List;

/**
 * 题目服务
 */
public interface QuestionService extends IService<Question> {
    
    Long addQuestion(QuestionAddDTO questionAddDTO);
    
    Boolean updateQuestion(Long id, QuestionUpdateDTO questionUpdateDTO);
    
    QuestionVO getQuestionById(Long id);
    
    Page<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO);

    /**
     * 从es中搜索题目
     * @param queryDTO
     * @return
     */
    Page<Question> searchFromEs(QuestionQueryDTO queryDTO);

    /**
     * 批量删除题目
     */
    boolean batchDeleteQuestions(List<Long> questionIds, Long operatorId);

    /**
     * 批量添加题目至题库
     */
    boolean batchAddQuestionsToRepo(List<Long> questionIds, Long repoId, Long operatorId);

    /**
     * 批量从题库移除题目
     */
    boolean batchRemoveQuestionsFromRepo(List<Long> questionIds, Long repoId, Long operatorId);
}
