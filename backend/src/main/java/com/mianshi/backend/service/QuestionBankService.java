package com.mianshi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mianshi.backend.model.entity.QuestionBank;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankAddDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankQueryDTO;
import com.mianshi.backend.model.vo.QuestionBank.QuestionBankVO;

/**
 * 题库服务
 */
public interface QuestionBankService extends IService<QuestionBank> {
    
    Long addQuestionBank(QuestionBankAddDTO questionBankAddDTO);
    
    Boolean updateQuestionBank(Long id, QuestionBankUpdateDTO questionBankUpdateDTO);
    
    QuestionBankVO getQuestionBankById(Long id);
    
    Page<QuestionBankVO> pageQuestionBanks(QuestionBankQueryDTO queryDTO);
}
