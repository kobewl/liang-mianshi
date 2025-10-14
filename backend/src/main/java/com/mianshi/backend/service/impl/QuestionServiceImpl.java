package com.mianshi.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.entity.Question;
import com.mianshi.backend.mapper.QuestionMapper;
import com.mianshi.backend.service.QuestionService;
import org.springframework.stereotype.Service;

/**
 * 题目服务实现
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

}
