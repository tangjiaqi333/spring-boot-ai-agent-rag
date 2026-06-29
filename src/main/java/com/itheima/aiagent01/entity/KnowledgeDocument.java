package com.itheima.aiagent01.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String embeddingJson;

    private LocalDateTime createdAt;

    private String sourceFileName;

    private String documentId;

    private Integer chunkIndex;

    private Integer totalChunks;

    public KnowledgeDocument() {
    }

    public KnowledgeDocument(String content) {
        this.content = content;
        this.sourceFileName = "manual";
        this.documentId = null;
        this.chunkIndex = 0;
        this.totalChunks = 1;
        this.createdAt = LocalDateTime.now();
    }

    public KnowledgeDocument(
            String content,
            String sourceFileName,
            String documentId,
            Integer chunkIndex,
            Integer totalChunks
    ) {
        this.content = content;
        this.sourceFileName = sourceFileName;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.createdAt = LocalDateTime.now();
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

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public Integer getTotalChunks() {
        return totalChunks;
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

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }
}