package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RerankServiceTest {

    private final DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);

    private final RerankService rerankService = new RerankService(deepSeekClient);

    @Test
    void shouldRerankCandidatesByModelReturnedIds() {
        KnowledgeSearchResult result1 = createResult(1L, "chunk 1", 10);
        KnowledgeSearchResult result2 = createResult(2L, "chunk 2", 20);
        KnowledgeSearchResult result3 = createResult(3L, "chunk 3", 30);

        List<KnowledgeSearchResult> candidates = List.of(result1, result2, result3);

        when(deepSeekClient.chat(anyList()))
                .thenReturn("[3,1,2]");

        List<KnowledgeSearchResult> reranked =
                rerankService.rerank("RAG 为什么需要切片？", candidates, 2);

        assertEquals(2, reranked.size());
        assertEquals(3L, reranked.get(0).getId());
        assertEquals(1L, reranked.get(1).getId());
    }

    @Test
    void shouldFallbackWhenModelReturnsInvalidText() {
        KnowledgeSearchResult result1 = createResult(1L, "chunk 1", 10);
        KnowledgeSearchResult result2 = createResult(2L, "chunk 2", 30);
        KnowledgeSearchResult result3 = createResult(3L, "chunk 3", 20);

        List<KnowledgeSearchResult> candidates = List.of(result1, result2, result3);

        when(deepSeekClient.chat(anyList()))
                .thenReturn("我觉得第二个最相关");

        List<KnowledgeSearchResult> reranked =
                rerankService.rerank("RAG 为什么需要切片？", candidates, 2);

        assertEquals(2, reranked.size());

        // fallback 会按照原始 score 从高到低排序
        assertEquals(2L, reranked.get(0).getId());
        assertEquals(3L, reranked.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenCandidatesEmpty() {
        List<KnowledgeSearchResult> reranked =
                rerankService.rerank("RAG 为什么需要切片？", new ArrayList<>(), 3);

        assertTrue(reranked.isEmpty());

        verify(deepSeekClient, never()).chat(anyList());
    }

    @Test
    void shouldReturnOriginalCandidatesWhenSizeLessThanTopK() {
        KnowledgeSearchResult result1 = createResult(1L, "chunk 1", 10);
        KnowledgeSearchResult result2 = createResult(2L, "chunk 2", 20);

        List<KnowledgeSearchResult> candidates = List.of(result1, result2);

        List<KnowledgeSearchResult> reranked =
                rerankService.rerank("RAG 为什么需要切片？", candidates, 3);

        assertEquals(2, reranked.size());
        assertEquals(1L, reranked.get(0).getId());
        assertEquals(2L, reranked.get(1).getId());

        verify(deepSeekClient, never()).chat(anyList());
    }

    private KnowledgeSearchResult createResult(Long id, String content, int score) {
        return new KnowledgeSearchResult(
                id,
                content,
                score,
                "test.txt",
                "doc-1",
                0
        );
    }
}