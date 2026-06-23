package com.itheima.aiagent01.dto;

import java.time.LocalDateTime;

public class KnowledgeDocumentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    public KnowledgeDocumentResponse() {
    }

    public KnowledgeDocumentResponse(Long id, String content, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
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
