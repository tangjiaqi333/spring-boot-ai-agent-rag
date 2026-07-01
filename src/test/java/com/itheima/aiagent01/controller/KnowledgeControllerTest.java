package com.itheima.aiagent01.controller;

import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import com.itheima.aiagent01.rag.KnowledgeBaseService;
import com.itheima.aiagent01.rag.RerankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

@WebMvcTest(KnowledgeController.class)
class KnowledgeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private KnowledgeBaseService knowledgeBaseService;
    @MockitoBean
    private RerankService rerankService;
    @Test
    void shouldReturnRagDebugResponse() throws Exception {
        KnowledgeSearchResult candidate1 = new KnowledgeSearchResult(
                1L,
                "RAG 需要切片是因为长文档太长。",
                39,
                "agent_chunk_test.txt",
                "doc-1",
                0
        );
        KnowledgeSearchResult candidate2 = new KnowledgeSearchResult(
                2L,
                "chunk 可以提高检索准确性。",
                32,
                "agent_chunk_test.txt",
                "doc-1",
                1
        );
        List<KnowledgeSearchResult> candidates = List.of(candidate1, candidate2);
        List<KnowledgeSearchResult> reranked = List.of(candidate1);
        when(knowledgeBaseService.vectorSearchWithScore(
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(candidates);

        when(rerankService.rerank(
                anyString(),
                anyList(),
                anyInt()
        )).thenReturn(reranked);

        mockMvc.perform(get("/api/knowledge/rag-debug")
                        .param("question", "RAG为什么需要切片"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.question").value("RAG为什么需要切片"))
                .andExpect(jsonPath("$.data.candidateCount").value(2))
                .andExpect(jsonPath("$.data.rerankedCount").value(1))
                .andExpect(jsonPath("$.data.candidates[0].id").value(1))
                .andExpect(jsonPath("$.data.reranked[0].id").value(1));
    }
}
