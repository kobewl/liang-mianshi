package com.mianshi.backend.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.mianshi.backend.mapper.QuestionMapper;
import com.mianshi.backend.model.dto.Question.QuestionEsDTO;
import com.mianshi.backend.model.entity.Question;
import com.mianshi.backend.model.esdao.QuestionEsDao;
import com.mianshi.backend.service.QuestionService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 增量同步面试题目到ElasticSearch的定时任务
 */
@Component
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionEsDao questionEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void run() {
        // 查询近 5 分钟内的数据
        long FIVE_MINUTES = 5 * 60 * 1000L;
        Date fiveMinutesData = new Date(new Date().getTime() - FIVE_MINUTES);

        List<Question> questionsList = questionMapper.listQuestionWithDeleted(fiveMinutesData);
        if (CollUtil.isEmpty(questionsList)) {
            log.info("IncSyncQuestionToEs no data, time:{}", fiveMinutesData);
            return;
        }

        // 转为 ES 实体类
        List<QuestionEsDTO> questionEsDTOLists = questionsList.stream()
                .map(QuestionEsDTO::toQuestionEsDTO)
                .collect(Collectors.toList());
        // 分页批量插入到 ES
        final int pageSize = 1000;
        int totalSize = questionEsDTOLists.size();
        log.info("IncSyncQuestionToEs start, totalSize:{}", totalSize);
        for (int i = 0; i < totalSize; i += pageSize) {
            int end = Math.min(totalSize, i + pageSize);
            log.info("IncSyncQuestionToEs, start:{}, end:{}", i, end);
            // 批量插入 ES
            questionEsDao.saveAll(questionEsDTOLists.subList(i, end));
        }
        log.info("IncSyncQuestionToEs success, totalSize:{}", totalSize);
    }
}
