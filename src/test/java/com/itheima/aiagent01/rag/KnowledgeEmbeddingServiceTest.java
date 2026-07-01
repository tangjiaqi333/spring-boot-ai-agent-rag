package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.service.EmbeddingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeEmbeddingServiceTest {

    private final EmbeddingService embeddingService = new EmbeddingService();

    private final KnowledgeEmbeddingService knowledgeEmbeddingService =
            new KnowledgeEmbeddingService(embeddingService);

    @Test
    void shouldConvertTextToEmbeddingJson() {
        String text = "RAG 为什么需要切片";

        String embeddingJson = knowledgeEmbeddingService.toEmbeddingJson(text);

        assertNotNull(embeddingJson);
        assertTrue(embeddingJson.startsWith("["));
        assertTrue(embeddingJson.endsWith("]"));
    }

    @Test
    void shouldConvertEmbeddingJsonBackToVector() {
        String text = "AI Agent";

        String embeddingJson = knowledgeEmbeddingService.toEmbeddingJson(text);

        double[] vector = knowledgeEmbeddingService.fromEmbeddingJson(embeddingJson);

        assertNotNull(vector);
        assertEquals(128, vector.length);
    }

    @Test
    void shouldReturnEmptyVectorWhenEmbeddingJsonIsBlank() {
        double[] vector = knowledgeEmbeddingService.fromEmbeddingJson("");

        assertNotNull(vector);
        assertEquals(0, vector.length);
    }

    @Test
    void shouldCalculateCosineSimilarity() {
        double[] vectorA = knowledgeEmbeddingService.embedText("RAG 为什么需要切片");
        double[] vectorB = knowledgeEmbeddingService.embedText("RAG 为什么需要切片");

        double similarity = knowledgeEmbeddingService.cosineSimilarity(vectorA, vectorB);

        assertTrue(similarity > 0.99);
    }
}