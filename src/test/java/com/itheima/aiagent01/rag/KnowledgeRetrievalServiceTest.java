package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import com.itheima.aiagent01.service.EmbeddingService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgeRetrievalServiceTest {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository =
            mock(KnowledgeDocumentRepository.class);

    private final EmbeddingService embeddingService = new EmbeddingService();

    private final KnowledgeEmbeddingService knowledgeEmbeddingService =
            new KnowledgeEmbeddingService(embeddingService);

    private final KnowledgeRetrievalService knowledgeRetrievalService =
            new KnowledgeRetrievalService(
                    knowledgeDocumentRepository,
                    knowledgeEmbeddingService
            );

    @Test
    void shouldReturnRelevantChunksWhenQuestionMatches() {
        KnowledgeDocument doc1 = new KnowledgeDocument(
                "RAG 的完整流程包括文档上传、文本切片、内容检索和模型回答。文档切片可以提高回答准确性。",
                "agent_chunk_test.txt",
                "doc-1",
                0,
                2
        );
        doc1.setId(1L);
        doc1.setEmbeddingJson(
                knowledgeEmbeddingService.toEmbeddingJson(doc1.getContent())
        );

        KnowledgeDocument doc2 = new KnowledgeDocument(
                "周杰伦有很多经典歌曲，比如晴天、七里香、稻香和青花瓷。",
                "music.txt",
                "doc-2",
                0,
                1
        );
        doc2.setId(2L);
        doc2.setEmbeddingJson(
                knowledgeEmbeddingService.toEmbeddingJson(doc2.getContent())
        );

        when(knowledgeDocumentRepository.findAll())
                .thenReturn(List.of(doc1, doc2));

        List<KnowledgeSearchResult> results =
                knowledgeRetrievalService.vectorSearchWithScore(
                        "RAG 为什么需要切片？",
                        3,
                        20
                );

        assertFalse(results.isEmpty());
        assertEquals(1L, results.get(0).getId());
        assertTrue(results.get(0).getScore() >= 20);
    }

    @Test
    void shouldReturnEmptyListWhenQuestionDoesNotMatch() {
        KnowledgeDocument doc = new KnowledgeDocument(
                "周杰伦有很多经典歌曲，比如晴天、七里香、稻香和青花瓷。",
                "music.txt",
                "doc-1",
                0,
                1
        );
        doc.setId(1L);
        doc.setEmbeddingJson(
                knowledgeEmbeddingService.toEmbeddingJson(doc.getContent())
        );

        when(knowledgeDocumentRepository.findAll())
                .thenReturn(List.of(doc));

        List<KnowledgeSearchResult> results =
                knowledgeRetrievalService.vectorSearchWithScore(
                        "RAG 为什么需要切片？",
                        3,
                        20
                );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldSkipDocumentWithoutEmbeddingJson() {
        KnowledgeDocument doc = new KnowledgeDocument(
                "RAG 的完整流程包括文档上传、文本切片、内容检索和模型回答。",
                "agent_chunk_test.txt",
                "doc-1",
                0,
                1
        );
        doc.setId(1L);
        doc.setEmbeddingJson(null);

        when(knowledgeDocumentRepository.findAll())
                .thenReturn(List.of(doc));

        List<KnowledgeSearchResult> results =
                knowledgeRetrievalService.vectorSearchWithScore(
                        "RAG 为什么需要切片？",
                        3,
                        20
                );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldRespectTopKLimit() {
        KnowledgeDocument doc1 = createDoc(1L, "RAG 文档切片可以提高检索准确率。", 0);
        KnowledgeDocument doc2 = createDoc(2L, "RAG chunk overlap 可以避免语义被截断。", 1);
        KnowledgeDocument doc3 = createDoc(3L, "RAG 检索会从知识库中召回相关内容。", 2);

        when(knowledgeDocumentRepository.findAll())
                .thenReturn(List.of(doc1, doc2, doc3));

        List<KnowledgeSearchResult> results =
                knowledgeRetrievalService.vectorSearchWithScore(
                        "RAG 为什么需要切片？",
                        2,
                        1
                );

        assertEquals(2, results.size());
    }

    private KnowledgeDocument createDoc(Long id, String content, int chunkIndex) {
        KnowledgeDocument doc = new KnowledgeDocument(
                content,
                "agent_chunk_test.txt",
                "doc-1",
                chunkIndex,
                3
        );
        doc.setId(id);
        doc.setEmbeddingJson(
                knowledgeEmbeddingService.toEmbeddingJson(content)
        );
        return doc;
    }
}