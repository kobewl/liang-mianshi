package com.mianshi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.mapper.QuestionBankMapper;
import com.mianshi.backend.model.entity.QuestionBank;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankAddDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankUpdateDTO;
import com.mianshi.backend.model.dto.QuestionBank.QuestionBankQueryDTO;
import com.mianshi.backend.service.QuestionBankService;
import com.mianshi.backend.model.vo.QuestionBank.QuestionBankVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Override
    public Long addQuestionBank(QuestionBankAddDTO questionBankAddDTO) {
        // 1. 参数校验
        if (questionBankAddDTO == null) {
            throw new IllegalArgumentException("题库信息不能为空");
        }
        
        // 2. 检查必填字段
        if (!StringUtils.hasText(questionBankAddDTO.getTitle())) {
            throw new IllegalArgumentException("题库标题不能为空");
        }
        
        // 3. 检查标题长度
        if (questionBankAddDTO.getTitle().length() < 2 || questionBankAddDTO.getTitle().length() > 100) {
            throw new IllegalArgumentException("题库标题长度必须在2-100个字符之间");
        }
        
        // 4. 检查描述长度（如果有描述）
        if (StringUtils.hasText(questionBankAddDTO.getDescription()) && 
            questionBankAddDTO.getDescription().length() > 500) {
            throw new IllegalArgumentException("题库描述不能超过500个字符");
        }
        
        // 5. 检查用户ID是否有效
        if (questionBankAddDTO.getUserId() != null && questionBankAddDTO.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        
        // 6. 转换并保存题库
        QuestionBank questionBank = BeanUtil.copyProperties(questionBankAddDTO, QuestionBank.class);
        this.save(questionBank);
        return questionBank.getId();
    }

    @Override
    public Boolean updateQuestionBank(Long id, QuestionBankUpdateDTO questionBankUpdateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("题库ID不能为空或小于等于0");
        }
        if (questionBankUpdateDTO == null) {
            throw new IllegalArgumentException("题库更新信息不能为空");
        }
        
        // 2. 检查题库是否存在
        QuestionBank existQuestionBank = this.getById(id);
        if (existQuestionBank == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        
        // 3. 如果更新标题，检查标题长度
        if (StringUtils.hasText(questionBankUpdateDTO.getTitle()) && 
            (questionBankUpdateDTO.getTitle().length() < 2 || questionBankUpdateDTO.getTitle().length() > 100)) {
            throw new IllegalArgumentException("题库标题长度必须在2-100个字符之间");
        }
        
        // 4. 如果更新描述，检查描述长度
        if (StringUtils.hasText(questionBankUpdateDTO.getDescription()) && 
            questionBankUpdateDTO.getDescription().length() > 500) {
            throw new IllegalArgumentException("题库描述不能超过500个字符");
        }
        
        // 5. 转换并更新题库
        QuestionBank questionBank = BeanUtil.copyProperties(questionBankUpdateDTO, QuestionBank.class);
        questionBank.setId(id);
        return this.updateById(questionBank);
    }

    @Override
    public QuestionBankVO getQuestionBankById(Long id) {
        QuestionBank questionBank = this.getById(id);
        if (questionBank == null) {
            return null;
        }
        return BeanUtil.copyProperties(questionBank, QuestionBankVO.class);
    }

    @Override
    public Page<QuestionBankVO> pageQuestionBanks(QuestionBankQueryDTO queryDTO) {
        LambdaQueryWrapper<QuestionBank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(queryDTO.getTitle()), QuestionBank::getTitle, queryDTO.getTitle());
        queryWrapper.eq(queryDTO.getUserId() != null, QuestionBank::getUserId, queryDTO.getUserId());
        
        queryWrapper.orderByAsc(QuestionBank::getId);
        queryWrapper.orderByAsc(QuestionBank::getCreateTime);
        
        Page<QuestionBank> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), queryWrapper);
        
        Page<QuestionBankVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(BeanUtil.copyToList(page.getRecords(), QuestionBankVO.class));
        return voPage;
    }
}
