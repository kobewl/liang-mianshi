package com.mianshi.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.entity.QuestionBank;
import com.mianshi.backend.mapper.QuestionBankMapper;
import com.mianshi.backend.service.QuestionBankService;
import org.springframework.stereotype.Service;

/**
 * 题库服务实现
 */
@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankService {

}
