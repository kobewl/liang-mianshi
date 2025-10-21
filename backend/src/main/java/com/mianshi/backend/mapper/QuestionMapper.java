package com.mianshi.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mianshi.backend.model.entity.Question;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询题目列表（包含已经被删除的题目）
     */
    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> listQuestionWithDeleted(Date minUpdateTime);

}
