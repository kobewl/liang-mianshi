package com.mianshi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.mapper.QuestionBankQuestionMapper;
import com.mianshi.backend.model.entity.QuestionBankQuestion;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionAddDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBankQuestion.QuestionBankQuestionQueryDTO;
import com.mianshi.backend.service.QuestionBankQuestionService;
import com.mianshi.backend.model.vo.QuestionBankQuestion.QuestionBankQuestionVO;
import org.springframework.stereotype.Service;

@Service
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Override
    public Long addQuestionBankQuestion(QuestionBankQuestionAddDTO questionBankQuestionAddDTO) {
        // 1. 参数校验
        if (questionBankQuestionAddDTO == null) {
            throw new IllegalArgumentException("题库题目关联信息不能为空");
        }
        
        // 2. 检查必填字段
        if (questionBankQuestionAddDTO.getQuestionBankId() == null || questionBankQuestionAddDTO.getQuestionBankId() <= 0) {
            throw new IllegalArgumentException("题库ID不能为空或小于等于0");
        }
        if (questionBankQuestionAddDTO.getQuestionId() == null || questionBankQuestionAddDTO.getQuestionId() <= 0) {
            throw new IllegalArgumentException("题目ID不能为空或小于等于0");
        }
        
        // 3. 检查用户ID是否有效
        if (questionBankQuestionAddDTO.getUserId() != null && questionBankQuestionAddDTO.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        
        // 4. 检查是否已存在相同的关联
        LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionBankQuestion::getQuestionBankId, questionBankQuestionAddDTO.getQuestionBankId())
                   .eq(QuestionBankQuestion::getQuestionId, questionBankQuestionAddDTO.getQuestionId());
        QuestionBankQuestion existRelation = this.getOne(queryWrapper);
        if (existRelation != null) {
            throw new IllegalArgumentException("该题目已存在于题库中");
        }
        
        // 5. 转换并保存题库题目关联
        QuestionBankQuestion questionBankQuestion = BeanUtil.copyProperties(questionBankQuestionAddDTO, QuestionBankQuestion.class);
        this.save(questionBankQuestion);
        return questionBankQuestion.getId();
    }

    @Override
    public Boolean updateQuestionBankQuestion(Long id, QuestionBankQuestionUpdateDTO questionBankQuestionUpdateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("题库题目关联ID不能为空或小于等于0");
        }
        if (questionBankQuestionUpdateDTO == null) {
            throw new IllegalArgumentException("更新信息不能为空");
        }
        
        // 2. 检查关联是否存在
        QuestionBankQuestion existingRelation = this.getById(id);
        if (existingRelation == null) {
            throw new IllegalArgumentException("题库题目关联不存在");
        }
        
        // 3. 检查更新的题库ID是否有效
        if (questionBankQuestionUpdateDTO.getQuestionBankId() != null && questionBankQuestionUpdateDTO.getQuestionBankId() <= 0) {
            throw new IllegalArgumentException("题库ID必须大于0");
        }
        
        // 4. 检查更新的题目ID是否有效
        if (questionBankQuestionUpdateDTO.getQuestionId() != null && questionBankQuestionUpdateDTO.getQuestionId() <= 0) {
            throw new IllegalArgumentException("题目ID必须大于0");
        }
        
        // 5. 检查更新的用户ID是否有效
        if (questionBankQuestionUpdateDTO.getUserId() != null && questionBankQuestionUpdateDTO.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        
        // 6. 如果更新了题库ID或题目ID，检查是否已存在相同的关联
        if ((questionBankQuestionUpdateDTO.getQuestionBankId() != null && !questionBankQuestionUpdateDTO.getQuestionBankId().equals(existingRelation.getQuestionBankId())) ||
            (questionBankQuestionUpdateDTO.getQuestionId() != null && !questionBankQuestionUpdateDTO.getQuestionId().equals(existingRelation.getQuestionId()))) {
            
            Long newQuestionBankId = questionBankQuestionUpdateDTO.getQuestionBankId() != null ? 
                questionBankQuestionUpdateDTO.getQuestionBankId() : existingRelation.getQuestionBankId();
            Long newQuestionId = questionBankQuestionUpdateDTO.getQuestionId() != null ? 
                questionBankQuestionUpdateDTO.getQuestionId() : existingRelation.getQuestionId();
                
            LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionBankQuestion::getQuestionBankId, newQuestionBankId)
                       .eq(QuestionBankQuestion::getQuestionId, newQuestionId)
                       .ne(QuestionBankQuestion::getId, id); // 排除当前记录
            QuestionBankQuestion existRelation = this.getOne(queryWrapper);
            if (existRelation != null) {
                throw new IllegalArgumentException("该题目已存在于目标题库中");
            }
        }
        
        // 7. 转换并更新题库题目关联
        QuestionBankQuestion questionBankQuestion = BeanUtil.copyProperties(questionBankQuestionUpdateDTO, QuestionBankQuestion.class);
        questionBankQuestion.setId(id);
        return this.updateById(questionBankQuestion);
    }

    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionById(Long id) {
        QuestionBankQuestion questionBankQuestion = this.getById(id);
        if (questionBankQuestion == null) {
            return null;
        }
        return BeanUtil.copyProperties(questionBankQuestion, QuestionBankQuestionVO.class);
    }

    @Override
    public Page<QuestionBankQuestionVO> pageQuestionBankQuestions(QuestionBankQuestionQueryDTO queryDTO) {
        LambdaQueryWrapper<QuestionBankQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(queryDTO.getQuestionBankId() != null, QuestionBankQuestion::getQuestionBankId, queryDTO.getQuestionBankId());
        queryWrapper.eq(queryDTO.getQuestionId() != null, QuestionBankQuestion::getQuestionId, queryDTO.getQuestionId());
        queryWrapper.eq(queryDTO.getUserId() != null, QuestionBankQuestion::getUserId, queryDTO.getUserId());
        
        queryWrapper.orderByAsc(QuestionBankQuestion::getId);
        queryWrapper.orderByAsc(QuestionBankQuestion::getCreateTime);
        
        Page<QuestionBankQuestion> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), queryWrapper);
        
        Page<QuestionBankQuestionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(BeanUtil.copyToList(page.getRecords(), QuestionBankQuestionVO.class));
        return voPage;
    }
}
