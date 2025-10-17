package com.mianshi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.mapper.QuestionMapper;
import com.mianshi.backend.model.dto.Question.QuestionAddDTO;
import com.mianshi.backend.model.dto.Question.QuestionQueryDTO;
import com.mianshi.backend.model.dto.Question.QuestionUpdateDTO;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.vo.Question.QuestionVO;
import com.mianshi.backend.service.QuestionBankQuestionService;
import com.mianshi.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionBankQuestionService questionBankQuestionService;

    @Override
    public Long addQuestion(QuestionAddDTO questionAddDTO) {
        // 1. 参数校验
        if (questionAddDTO == null) {
            throw new IllegalArgumentException("题目信息不能为空");
        }

        // 2. 检查必填字段
        if (!StringUtils.hasText(questionAddDTO.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }

        // 3. 检查标题长度
        if (questionAddDTO.getTitle().length() < 2 || questionAddDTO.getTitle().length() > 100) {
            throw new IllegalArgumentException("题目标题长度必须在2-100个字符之间");
        }

        // 4. 检查内容长度（如果有内容）
        if (StringUtils.hasText(questionAddDTO.getContent()) &&
                questionAddDTO.getContent().length() > 5000) {
            throw new IllegalArgumentException("题目内容不能超过5000个字符");
        }

        // 5. 检查用户ID是否有效
        if (questionAddDTO.getUserId() != null && questionAddDTO.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }

        // 6. 校验题目难度，默认基础难度
        Integer difficulty = questionAddDTO.getDifficulty();
        if (difficulty == null) {
            questionAddDTO.setDifficulty(1);
        } else if (!isValidDifficulty(difficulty)) {
            throw new IllegalArgumentException("题目难度取值范围应为1-4");
        }

        // 7. 转换并保存题目
        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO, question, CopyOptions.create().setIgnoreProperties("tags"));
        question.setTags(serializeTags(questionAddDTO.getTags()));
        this.save(question);
        return question.getId();
    }

    @Override
    public Boolean updateQuestion(Long id, QuestionUpdateDTO questionUpdateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("题目ID不能为空或小于等于0");
        }
        if (questionUpdateDTO == null) {
            throw new IllegalArgumentException("题目更新信息不能为空");
        }

        // 2. 检查题目是否存在
        Question existQuestion = this.getById(id);
        if (existQuestion == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        // 3. 如果更新标题，检查标题长度
        if (StringUtils.hasText(questionUpdateDTO.getTitle()) &&
                (questionUpdateDTO.getTitle().length() < 2 || questionUpdateDTO.getTitle().length() > 100)) {
            throw new IllegalArgumentException("题目标题长度必须在2-100个字符之间");
        }

        // 4. 如果更新内容，检查内容长度
        if (StringUtils.hasText(questionUpdateDTO.getContent()) &&
                questionUpdateDTO.getContent().length() > 5000) {
            throw new IllegalArgumentException("题目内容不能超过5000个字符");
        }

        // 5. 如果更新难度，校验范围
        if (questionUpdateDTO.getDifficulty() != null && !isValidDifficulty(questionUpdateDTO.getDifficulty())) {
            throw new IllegalArgumentException("题目难度取值范围应为1-4");
        }

        // 6. 转换并更新题目
        Question question = new Question();
        BeanUtil.copyProperties(questionUpdateDTO, question, CopyOptions.create().setIgnoreProperties("tags"));
        question.setId(id);
        if (question.getDifficulty() == null) {
            question.setDifficulty(existQuestion.getDifficulty());
        }
        question.setTags(serializeTags(questionUpdateDTO.getTags()));
        return this.updateById(question);
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = this.getById(id);
        if (question == null) {
            return null;
        }
        return toQuestionVO(question);
    }

    @Override
    public Page<QuestionVO> pageQuestions(QuestionQueryDTO queryDTO) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了题库ID，先获取该题库的题目ID列表
        if (queryDTO.getQuestionBankId() != null) {
            List<Long> questionIds = questionBankQuestionService.list(
                            new LambdaQueryWrapper<com.mianshi.backend.model.entity.QuestionBankQuestion>()
                                    .eq(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionBankId,
                                            queryDTO.getQuestionBankId())
                                    .select(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionId))
                    .stream().map(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if (questionIds.isEmpty()) {
                // 如果题库中没有题目，返回空结果
                Page<QuestionVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize(), 0);
                voPage.setRecords(Collections.emptyList());
                return voPage;
            } else {
                queryWrapper.in(Question::getId, questionIds);
            }
        }

        queryWrapper.like(StringUtils.hasText(queryDTO.getTitle()), Question::getTitle, queryDTO.getTitle());
        queryWrapper.like(StringUtils.hasText(queryDTO.getTags()), Question::getTags, queryDTO.getTags());
        queryWrapper.eq(queryDTO.getUserId() != null, Question::getUserId, queryDTO.getUserId());
        queryWrapper.eq(queryDTO.getDifficulty() != null, Question::getDifficulty, queryDTO.getDifficulty());

        queryWrapper.orderByAsc(Question::getId);
        queryWrapper.orderByAsc(Question::getCreateTime);

        Page<Question> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), queryWrapper);

        Page<QuestionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::toQuestionVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    private QuestionVO toQuestionVO(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        BeanUtil.copyProperties(question, vo, CopyOptions.create().setIgnoreProperties("tags"));
        vo.setTags(deserializeTags(question.getTags()));
        return vo;
    }

    private String serializeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }
        List<String> normalized = tags.stream()
                .map(tag -> tag == null ? null : tag.trim())
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(normalized);
    }

    private List<String> deserializeTags(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(tagsJson), String.class)
                    .stream()
                    .map(tag -> tag == null ? null : tag.trim())
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Arrays.stream(tagsJson.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private boolean isValidDifficulty(Integer difficulty) {
        return difficulty != null && difficulty >= 1 && difficulty <= 4;
    }
}
