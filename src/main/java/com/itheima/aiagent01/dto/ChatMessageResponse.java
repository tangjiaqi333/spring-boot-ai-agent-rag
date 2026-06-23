package com.itheima.aiagent01.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(String role, String context, LocalDateTime createdAt) {
        this.role = role;
        this.context = context;
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private String role;
    private String context;
    private LocalDateTime createdAt;
}
