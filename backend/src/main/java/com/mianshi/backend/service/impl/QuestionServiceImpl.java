package com.mianshi.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mianshi.backend.mapper.QuestionMapper;
import com.mianshi.backend.model.dto.Question.QuestionAddDTO;
import com.mianshi.backend.model.dto.Question.QuestionEsDTO;
import com.mianshi.backend.model.dto.Question.QuestionQueryDTO;
import com.mianshi.backend.model.dto.Question.QuestionUpdateDTO;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.entity.QuestionBank;
import com.mianshi.backend.model.entity.QuestionBankQuestion;
import com.mianshi.backend.model.vo.Question.QuestionVO;
import com.mianshi.backend.service.QuestionBankQuestionService;
import com.mianshi.backend.service.QuestionBankService;
import com.mianshi.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final QuestionBankQuestionService questionBankQuestionService;
    private final QuestionBankService questionBankService;
    private final ElasticsearchTemplate elasticsearchTemplate;

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

    @Override
    public Page<Question> searchFromEs(QuestionQueryDTO queryDTO) {

        // ===== 1) 构建 BoolQuery=====
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 如果指定了题库 ID，则先获取关联的题目 ID，用于过滤
        List<Long> scopedQuestionIds = null;
        if (queryDTO.getQuestionBankId() != null) {
            scopedQuestionIds = questionBankQuestionService.list(
                            new LambdaQueryWrapper<com.mianshi.backend.model.entity.QuestionBankQuestion>()
                                    .eq(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionBankId,
                                            queryDTO.getQuestionBankId())
                                    .select(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionId))
                    .stream()
                    .map(com.mianshi.backend.model.entity.QuestionBankQuestion::getQuestionId)
                    .collect(Collectors.toList());

            if (scopedQuestionIds.isEmpty()) {
                Page<Question> emptyPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            List<FieldValue> idValues = scopedQuestionIds.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());
            filterQueries.add(Query.of(q -> q.terms(t -> t.field("id").terms(tf -> tf.value(idValues)))));
        }

        if (StringUtils.hasText(queryDTO.getSearchText())) {
            mustQueries.add(Query.of(q -> q.multiMatch(mm -> mm
                    .query(queryDTO.getSearchText())
                    .fields("title^4", "content^2", "answer")
            )));
        }

        if (StringUtils.hasText(queryDTO.getTitle())) {
            mustQueries.add(Query.of(q -> q.matchPhrase(mp -> mp.field("title").query(queryDTO.getTitle()))));
        }

        if (StringUtils.hasText(queryDTO.getTags())) {
            List<String> tags = Arrays.stream(queryDTO.getTags().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
            if (!tags.isEmpty()) {
                List<FieldValue> tagValues = tags.stream().map(FieldValue::of).collect(Collectors.toList());
                filterQueries.add(Query.of(q -> q.terms(t -> t.field("tags").terms(tf -> tf.value(tagValues)))));
            }
        }

        if (queryDTO.getDifficulty() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("difficulty").value(queryDTO.getDifficulty()))));
        }

        if (queryDTO.getUserId() != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field("userId").value(queryDTO.getUserId()))));
        }

        if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
            mustQueries.add(Query.of(q -> q.matchAll(ma -> ma)));
        }

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (!mustQueries.isEmpty()) {
            bool.must(mustQueries);
        }
        if (!filterQueries.isEmpty()) {
            bool.filter(filterQueries);
        }

        // ===== 2) 分页 + 排序 =====
        int currentIdx = Math.max(0, queryDTO.getCurrent() - 1);
        int size = Math.max(1, queryDTO.getSize());
        Sort sort = StringUtils.hasText(queryDTO.getSortField())
                ? Sort.by(("desc".equalsIgnoreCase(queryDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC),
                queryDTO.getSortField())
                : Sort.by(Sort.Direction.DESC, "createTime");

        HighlightParameters hlParams = HighlightParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withFragmentSize(120)
                .withNumberOfFragments(1)
                .build();

        Highlight hl = new Highlight(
                hlParams,  // 先 parameters
                java.util.List.of(
                        new HighlightField("title"),
                        new HighlightField("content"),
                        new HighlightField("answer")
                )
        );

        HighlightQuery hlQuery = new HighlightQuery(hl, QuestionEsDTO.class);

        // ===== 4) 组装查询 =====
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool.build()))
                .withPageable(PageRequest.of(currentIdx, size))
                .withSort(sort)
                .withHighlightQuery(hlQuery)   // ★ 开启高亮
                .build();

        // ===== 5) 执行查询 =====
        SearchHits<QuestionEsDTO> hits = elasticsearchTemplate.search(query, QuestionEsDTO.class);

        // ===== 6) 命中转换 + 高亮回填 =====
        java.util.List<Question> records = new java.util.ArrayList<>();
        for (SearchHit<QuestionEsDTO> hit : hits) {
            QuestionEsDTO es = hit.getContent();

            // 取高亮结果（每个字段可能有多段，常用第一段）
            java.util.Map<String, java.util.List<String>> hlMap = hit.getHighlightFields();
            if (hlMap != null && !hlMap.isEmpty()) {
                es.setTitle(firstOrElse(hlMap.get("title"), es.getTitle()));
                es.setContent(firstOrElse(hlMap.get("content"), es.getContent()));
                es.setAnswer(firstOrElse(hlMap.get("answer"), es.getAnswer()));
            }

            records.add(QuestionEsDTO.dtoToObj(es));
        }

        if (!records.isEmpty()) {
            Map<Long, Question> dbQuestionMap = this.listByIds(
                    records.stream()
                            .map(Question::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            ).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Question::getId, q -> q));

            records.forEach(record -> {
                Question dbQuestion = dbQuestionMap.get(record.getId());
                if (dbQuestion != null) {
                    record.setCreateTime(dbQuestion.getCreateTime());
                    record.setUpdateTime(dbQuestion.getUpdateTime());
                }
            });
        }

        // ===== 7) 封装为 MyBatis-Plus 的 Page =====
        Page<Question> page = new Page<>(queryDTO.getCurrent(), size);
        page.setTotal(hits.getTotalHits());
        page.setRecords(records);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteQuestions(List<Long> questionIds, Long operatorId) {
        List<Long> normalizedIds = normalizeQuestionIds(questionIds);
        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("题目ID列表不能为空");
        }
        validateOperator(operatorId);
        ensureQuestionsExist(normalizedIds);

        questionBankQuestionService.remove(
                new LambdaQueryWrapper<QuestionBankQuestion>()
                        .in(QuestionBankQuestion::getQuestionId, normalizedIds)
        );

        if (!this.removeBatchByIds(normalizedIds)) {
            throw new IllegalStateException("批量删除题目失败，请稍后再试");
        }
        log.info("operator {} batch deleted questions {}", operatorId, normalizedIds);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddQuestionsToRepo(List<Long> questionIds, Long repoId, Long operatorId) {
        List<Long> normalizedIds = normalizeQuestionIds(questionIds);
        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("题目ID列表不能为空");
        }
        validateOperator(operatorId);
        QuestionBank questionBank = requireQuestionBank(repoId);
        ensureQuestionsExist(normalizedIds);

        Set<Long> existingQuestionIds = questionBankQuestionService.list(
                        new LambdaQueryWrapper<QuestionBankQuestion>()
                                .eq(QuestionBankQuestion::getQuestionBankId, questionBank.getId())
                                .in(QuestionBankQuestion::getQuestionId, normalizedIds)
                                .select(QuestionBankQuestion::getQuestionId))
                .stream()
                .map(QuestionBankQuestion::getQuestionId)
                .collect(Collectors.toCollection(HashSet::new));

        List<QuestionBankQuestion> relationsToSave = normalizedIds.stream()
                .filter(id -> !existingQuestionIds.contains(id))
                .map(id -> {
                    QuestionBankQuestion relation = new QuestionBankQuestion();
                    relation.setQuestionBankId(questionBank.getId());
                    relation.setQuestionId(id);
                    relation.setUserId(operatorId);
                    return relation;
                })
                .collect(Collectors.toList());

        if (!relationsToSave.isEmpty() && !questionBankQuestionService.saveBatch(relationsToSave, 200)) {
            throw new IllegalStateException("批量添加题目到题库失败，请稍后再试");
        }
        log.info("operator {} added questions {} into repo {}", operatorId, normalizedIds, questionBank.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchRemoveQuestionsFromRepo(List<Long> questionIds, Long repoId, Long operatorId) {
        List<Long> normalizedIds = normalizeQuestionIds(questionIds);
        if (normalizedIds.isEmpty()) {
            throw new IllegalArgumentException("题目ID列表不能为空");
        }
        validateOperator(operatorId);
        QuestionBank questionBank = requireQuestionBank(repoId);
        ensureQuestionsExist(normalizedIds);

        List<QuestionBankQuestion> existingRelations = questionBankQuestionService.list(
                new LambdaQueryWrapper<QuestionBankQuestion>()
                        .eq(QuestionBankQuestion::getQuestionBankId, questionBank.getId())
                        .in(QuestionBankQuestion::getQuestionId, normalizedIds)
        );

        if (existingRelations.isEmpty()) {
            log.info("operator {} attempted to remove questions {} from repo {} but no relations found",
                    operatorId, normalizedIds, questionBank.getId());
            return true;
        }

        List<Long> relationIds = existingRelations.stream()
                .map(QuestionBankQuestion::getId)
                .collect(Collectors.toList());

        if (!questionBankQuestionService.removeBatchByIds(relationIds)) {
            throw new IllegalStateException("批量从题库移除题目失败，请稍后再试");
        }
        log.info("operator {} removed questions {} from repo {}", operatorId, normalizedIds, questionBank.getId());
        return true;
    }

    private static String firstOrElse(java.util.List<String> list, String fallback) {
        return (list != null && !list.isEmpty() && StringUtils.hasText(list.get(0))) ? list.get(0) : fallback;
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

    private void validateOperator(Long operatorId) {
        if (operatorId == null || operatorId <= 0) {
            throw new IllegalArgumentException("操作人ID无效");
        }
    }

    private List<Long> normalizeQuestionIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return questionIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private void ensureQuestionsExist(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return;
        }
        Set<Long> existIds = this.listByIds(questionIds).stream()
                .filter(Objects::nonNull)
                .map(Question::getId)
                .collect(Collectors.toSet());
        if (existIds.size() != questionIds.size()) {
            List<Long> missing = questionIds.stream()
                    .filter(id -> !existIds.contains(id))
                    .collect(Collectors.toList());
            throw new IllegalArgumentException("以下题目不存在或已删除：" + missing);
        }
    }

    private QuestionBank requireQuestionBank(Long repoId) {
        if (repoId == null || repoId <= 0) {
            throw new IllegalArgumentException("题库ID不能为空或无效");
        }
        QuestionBank questionBank = questionBankService.getById(repoId);
        if (questionBank == null) {
            throw new IllegalArgumentException("题库不存在或已删除：" + repoId);
        }
        return questionBank;
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
