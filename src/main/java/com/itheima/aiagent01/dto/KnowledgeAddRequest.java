package com.itheima.aiagent01.dto;

import jakarta.validation.constraints.NotBlank;

public class KnowledgeAddRequest {
    @NotBlank(message = "content cannot be empty")
    private String content;

    public KnowledgeAddRequest() {
    }

    public KnowledgeAddRequest(String content) {
        this.content = content;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
