package com.itheima.aiagent01.rag;

import com.itheima.aiagent01.dto.KnowledgeDocumentResponse;
import com.itheima.aiagent01.dto.KnowledgeDocumentSummaryResponse;
import com.itheima.aiagent01.dto.KnowledgeSearchResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
//统一接口
@Service
public class KnowledgeBaseService {

    private final KnowledgeDocumentService knowledgeDocumentService;
    private final KnowledgeRetrievalService knowledgeRetrievalService;

    public KnowledgeBaseService(
            KnowledgeDocumentService knowledgeDocumentService,
            KnowledgeRetrievalService knowledgeRetrievalService
    ) {
        this.knowledgeDocumentService = knowledgeDocumentService;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
    }

    public void addDocument(String content) {
        knowledgeDocumentService.addDocument(content);
    }

    public List<KnowledgeDocumentResponse> listDocuments() {
        return knowledgeDocumentService.listDocuments();
    }

    public boolean deleteDocument(Long id) {
        return knowledgeDocumentService.deleteDocument(id);
    }

    public boolean updateDocument(Long id, String content) {
        return knowledgeDocumentService.updateDocument(id, content);
    }

    public void uploadDocument(MultipartFile file) {
        knowledgeDocumentService.uploadDocument(file);
    }

    public boolean deleteByDocumentId(String documentId) {
        return knowledgeDocumentService.deleteByDocumentId(documentId);
    }

    public List<KnowledgeDocumentSummaryResponse> listDocumentSummaries() {
        return knowledgeDocumentService.listDocumentSummaries();
    }

    public List<KnowledgeSearchResult> vectorSearchWithScore(String question, int topK) {
        return vectorSearchWithScore(question, topK, 20);
    }

    public List<KnowledgeSearchResult> vectorSearchWithScore(String question, int topK, int minScore) {
        return knowledgeRetrievalService.vectorSearchWithScore(question, topK, minScore);
    }

    public List<KnowledgeSearchResult> searchWithScore(String question, int topK) {
        return knowledgeRetrievalService.vectorSearchWithScore(question, topK, 20);
    }

    public List<String> search(String question) {
        return search(question, 3);
    }

    public List<String> search(String question, int topK) {
        List<KnowledgeSearchResult> searchResults =
                knowledgeRetrievalService.vectorSearchWithScore(question, topK, 20);

        List<String> contents = new ArrayList<>();

        for (KnowledgeSearchResult result : searchResults) {
            contents.add(result.getContent());
        }

        return contents;
    }
}