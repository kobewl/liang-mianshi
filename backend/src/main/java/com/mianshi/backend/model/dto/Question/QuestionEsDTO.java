package com.mianshi.backend.model.dto.Question;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.mianshi.backend.model.entity.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目添加 DTO
 */
@Document(indexName = "question")
@Data
public class QuestionEsDTO implements Serializable {

    private static final String DATA_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Id
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目难度（1-基础 2-简单 3-中等 4-困难）
     */
    private Integer difficulty;

    /**
     * 答案
     */
    private String answer;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATA_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = DATA_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;


    /**
     * 对象转包装类
     */
    public static QuestionEsDTO toQuestionEsDTO(Question question) {
        if (question == null) {
            return null;
        }
        QuestionEsDTO questionEsDTO = new QuestionEsDTO();
        BeanUtils.copyProperties(question, questionEsDTO);
        String tagsStr = question.getTags();
        if (tagsStr != null) {
            questionEsDTO.setTags(JSONUtil.toList(tagsStr, String.class));
        }
        return questionEsDTO;
    }

    /**
     * 对象转包装类
     */
    public static Question dtoToObj(QuestionEsDTO questionEsDTO) {
        if (questionEsDTO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEsDTO, question);
        List<String> tags = questionEsDTO.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        return question;

    }

}
