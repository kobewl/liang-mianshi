package com.mianshi.backend.job.once;

import cn.hutool.core.collection.CollUtil;
import com.mianshi.backend.model.dto.Question.QuestionEsDTO;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.esdao.QuestionEsDao;
import com.mianshi.backend.service.QuestionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FullSyncQuestionToEs类是一个Spring组件，用于实现将问题数据同步到ElasticSearch搜索引擎的功能。
 * 该类实现了CommandLineRunner接口，确保在Spring Boot应用程序启动后自动执行同步操作。
 */
//@Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    /**
     * run方法是CommandLineRunner接口的实现方法，它将在应用程序启动后自动调用。
     * 该方法负责执行将问题数据同步到ElasticSearch的逻辑。
     *
     * @param args 命令行参数，可以在启动应用程序时传入
     * @throws Exception 如果同步过程中发生任何错误，将抛出异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 全量获取题目
        List<Question> questionsList = questionService.list();
        if (CollUtil.isEmpty(questionsList)) {
            log.info("questionList is empty");
            return;
        }
        // 转为 ES 实体类
        List<QuestionEsDTO> questionEsDTOLists = questionsList.stream()
                .map(QuestionEsDTO::toQuestionEsDTO)
                .collect(Collectors.toList());
        // 分页批量插入到 ES
        final int pageSize = 1000;
        int totalSize = questionEsDTOLists.size();
        log.info("FullSyncQuestionToEs start, totalSize:{}", totalSize);
        for (int i = 0; i < totalSize; i += pageSize) {
            int end = Math.min(totalSize, i + pageSize);
            log.info("FullSyncQuestionToEs, start:{}, end:{}", i, end);
            // 批量插入 ES
            questionEsDao.saveAll(questionEsDTOLists.subList(i, end));
        }
        log.info("FullSyncQuestionToEs success, totalSize:{}", totalSize);
    }
}
