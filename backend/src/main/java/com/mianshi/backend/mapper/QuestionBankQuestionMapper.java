package com.mianshi.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mianshi.backend.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库题目关联 Mapper
 */
@Mapper
public interface QuestionBankQuestionMapper extends BaseMapper<QuestionBankQuestion> {

}
