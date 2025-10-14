package com.mianshi.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.entity.QuestionBankQuestion;
import com.mianshi.backend.mapper.QuestionBankQuestionMapper;
import com.mianshi.backend.service.QuestionBankQuestionService;
import org.springframework.stereotype.Service;

/**
 * 题库题目关联服务实现
 */
@Service
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion>
        implements QuestionBankQuestionService {

}
