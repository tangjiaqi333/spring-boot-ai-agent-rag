package com.itheima.aiagent01.dto;

import java.time.LocalDateTime;

public class KnowledgeDocumentSummaryResponse {
    private String documentId;
    private String sourceFileName;
    private Integer totalChunks;
    private LocalDateTime createdAt;

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }




    public KnowledgeDocumentSummaryResponse() {
    }



    public KnowledgeDocumentSummaryResponse(String documentId, String sourceFileName, Integer totalChunks, LocalDateTime createdAt) {
        this.documentId = documentId;
        this.sourceFileName = sourceFileName;
        this.totalChunks = totalChunks;
        this.createdAt = createdAt;
    }




}
