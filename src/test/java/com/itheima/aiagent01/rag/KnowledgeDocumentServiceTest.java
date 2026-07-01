package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeDocumentSummaryResponse;
import com.itheima.aiagent01.entity.KnowledgeDocument;
import com.itheima.aiagent01.repository.KnowledgeDocumentRepository;
import com.itheima.aiagent01.service.EmbeddingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgeDocumentServiceTest {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository =
            mock(KnowledgeDocumentRepository.class);

    private final DocumentChunkService documentChunkService =
            new DocumentChunkService();

    private final EmbeddingService embeddingService =
            new EmbeddingService();

    private final KnowledgeEmbeddingService knowledgeEmbeddingService =
            new KnowledgeEmbeddingService(embeddingService);

    private final KnowledgeDocumentService knowledgeDocumentService =
            new KnowledgeDocumentService(
                    knowledgeDocumentRepository,
                    documentChunkService,
                    knowledgeEmbeddingService
            );

    @Test
    void shouldAddDocumentWithEmbeddingJson() {
        knowledgeDocumentService.addDocument("RAG 是检索增强生成");

        ArgumentCaptor<KnowledgeDocument> captor =
                ArgumentCaptor.forClass(KnowledgeDocument.class);

        verify(knowledgeDocumentRepository).save(captor.capture());

        KnowledgeDocument saved = captor.getValue();

        assertEquals("RAG 是检索增强生成", saved.getContent());
        assertNotNull(saved.getEmbeddingJson());
        assertTrue(saved.getEmbeddingJson().startsWith("["));
        assertTrue(saved.getEmbeddingJson().endsWith("]"));
    }

    @Test
    void shouldUpdateDocumentAndRefreshEmbeddingJson() {
        KnowledgeDocument document = new KnowledgeDocument("旧内容");

        when(knowledgeDocumentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        boolean success = knowledgeDocumentService.updateDocument(1L, "新内容 RAG");

        assertTrue(success);
        assertEquals("新内容 RAG", document.getContent());
        assertNotNull(document.getEmbeddingJson());

        verify(knowledgeDocumentRepository).save(document);
    }

    @Test
    void shouldReturnFalseWhenUpdateDocumentNotFound() {
        when(knowledgeDocumentRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean success = knowledgeDocumentService.updateDocument(999L, "新内容");

        assertFalse(success);
        verify(knowledgeDocumentRepository, never()).save(any());
    }

    @Test
    void shouldDeleteDocumentById() {
        when(knowledgeDocumentRepository.existsById(1L))
                .thenReturn(true);

        boolean success = knowledgeDocumentService.deleteDocument(1L);

        assertTrue(success);
        verify(knowledgeDocumentRepository).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeleteDocumentNotExists() {
        when(knowledgeDocumentRepository.existsById(1L))
                .thenReturn(false);

        boolean success = knowledgeDocumentService.deleteDocument(1L);

        assertFalse(success);
        verify(knowledgeDocumentRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteByDocumentId() {
        KnowledgeDocument doc1 = new KnowledgeDocument(
                "chunk 1",
                "test.txt",
                "doc-1",
                0,
                2
        );

        KnowledgeDocument doc2 = new KnowledgeDocument(
                "chunk 2",
                "test.txt",
                "doc-1",
                1,
                2
        );

        when(knowledgeDocumentRepository.findByDocumentId("doc-1"))
                .thenReturn(List.of(doc1, doc2));

        boolean success = knowledgeDocumentService.deleteByDocumentId("doc-1");

        assertTrue(success);
        verify(knowledgeDocumentRepository).deleteAll(List.of(doc1, doc2));
    }

    @Test
    void shouldUploadTxtFileAndSaveChunksWithEmbeddingJson() {
        String content = "a".repeat(1200);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "agent_chunk_test.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );

        knowledgeDocumentService.uploadDocument(file);

        ArgumentCaptor<KnowledgeDocument> captor =
                ArgumentCaptor.forClass(KnowledgeDocument.class);

        verify(knowledgeDocumentRepository, times(3))
                .save(captor.capture());

        List<KnowledgeDocument> savedDocuments = captor.getAllValues();

        assertEquals(3, savedDocuments.size());

        String documentId = savedDocuments.get(0).getDocumentId();

        assertNotNull(documentId);

        for (int i = 0; i < savedDocuments.size(); i++) {
            KnowledgeDocument document = savedDocuments.get(i);

            assertEquals("agent_chunk_test.txt", document.getSourceFileName());
            assertEquals(documentId, document.getDocumentId());
            assertEquals(i, document.getChunkIndex());
            assertEquals(3, document.getTotalChunks());

            assertNotNull(document.getEmbeddingJson());
            assertTrue(document.getEmbeddingJson().startsWith("["));
            assertTrue(document.getEmbeddingJson().endsWith("]"));
        }
    }

    @Test
    void shouldThrowExceptionWhenUploadUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> knowledgeDocumentService.uploadDocument(file)
        );

        assertEquals("目前只支持 txt 和 md 文件", exception.getMessage());
    }

    @Test
    void shouldListDocumentSummariesByDocumentId() {
        KnowledgeDocument doc1 = new KnowledgeDocument(
                "chunk 1",
                "test.txt",
                "doc-1",
                0,
                2
        );

        KnowledgeDocument doc2 = new KnowledgeDocument(
                "chunk 2",
                "test.txt",
                "doc-1",
                1,
                2
        );

        KnowledgeDocument doc3 = new KnowledgeDocument(
                "chunk 1",
                "other.txt",
                "doc-2",
                0,
                1
        );

        when(knowledgeDocumentRepository.findAll())
                .thenReturn(List.of(doc1, doc2, doc3));

        List<KnowledgeDocumentSummaryResponse> summaries =
                knowledgeDocumentService.listDocumentSummaries();

        assertEquals(2, summaries.size());
        assertEquals("doc-1", summaries.get(0).getDocumentId());
        assertEquals("doc-2", summaries.get(1).getDocumentId());
    }
}