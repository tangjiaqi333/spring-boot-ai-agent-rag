package com.itheima.aiagent01.dto;

import jakarta.validation.constraints.NotBlank;

public class KnowledgeUpdateRequest {

    @NotBlank(message = "content cannot be empty")
    private String content;

    public KnowledgeUpdateRequest () {
    }

    public KnowledgeUpdateRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
