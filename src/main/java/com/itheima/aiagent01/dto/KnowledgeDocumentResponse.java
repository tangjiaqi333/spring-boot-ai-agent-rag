package com.itheima.aiagent01.dto;

import java.time.LocalDateTime;

public class KnowledgeDocumentResponse {
    private Long id;

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

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    private String content;
    private LocalDateTime createdAt;
    private String sourceFileName;
    private String documentId;
    private Integer chunkIndex;
    private Integer totalChunks;

    public KnowledgeDocumentResponse() {
    }

    public KnowledgeDocumentResponse(
            Long id,
            String content,
            LocalDateTime createdAt,
            String sourceFileName,
            String documentId,
            Integer chunkIndex,
            Integer totalChunks
    ) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.sourceFileName = sourceFileName;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }






}
