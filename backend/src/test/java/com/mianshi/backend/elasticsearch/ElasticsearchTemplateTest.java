package com.mianshi.backend.elasticsearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class ElasticsearchTemplateTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    private final String INDEX_NAME = "test_index";

    // Index (Create) a document
    @Test
    public void indexDocument() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", "Elasticsearch Introduction");
        doc.put("content", "Learn Elasticsearch basics and advanced usage.");
        doc.put("tags", "elasticsearch,search");
        doc.put("answer", "Yes");
        doc.put("userId", 1L);
        doc.put("editTime", "2023-09-01 10:00:00");
        doc.put("createTime", "2023-09-01 09:10:00");
        doc.put("updateTime", "2023-09-01 09:10:00");
        doc.put("isDelete", false);

        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId("1")
                .withObject(doc)
                .build();

        String documentId = elasticsearchTemplate.index(indexQuery, IndexCoordinates.of(INDEX_NAME));

        assertThat(documentId).isNotNull();
    }

    @Test
    public void getDocument() {
        String documentId = "1";
        Map<String, Object> document = elasticsearchTemplate.get(documentId, Map.class,
                IndexCoordinates.of(INDEX_NAME));
        System.out.println(document);
        assertThat(document).isNotNull();
        assertThat(document.get("title")).isEqualTo("Elasticsearch Introduction");
    }

    // update document
    @Test
    public void updateDocument() {
        String documentId = "1";
        Map<String, Object> document = new HashMap<>();
        document.put("title", "Elasticsearch Introduction Updated");

        UpdateQuery updateQuery = UpdateQuery.builder(documentId)
                .withDocument(Document.from(document))
                .build();
        elasticsearchTemplate.update(document, IndexCoordinates.of(INDEX_NAME));

        Map<String, Object> updatedDocument = elasticsearchTemplate.get(documentId, Map.class,
                IndexCoordinates.of(INDEX_NAME));
        assertThat(updatedDocument).isNotNull();
        assertThat(updatedDocument.get("title")).isEqualTo("Elasticsearch Introduction Updated");
    }

    // delete document
    @Test
    public void deleteDocument() {
        String documentId = "1";
        elasticsearchTemplate.delete(documentId, IndexCoordinates.of(INDEX_NAME));
        Map<String, Object> document = elasticsearchTemplate.get(documentId, Map.class,
                IndexCoordinates.of(INDEX_NAME));
        assertThat(document).isNull();
    }

}
