package com.mianshi.backend.model.esdao;

import com.mianshi.backend.model.dto.Question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 问题数据访问对象类
 * 该类用于处理与问题相关的数据库操作
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {
}
