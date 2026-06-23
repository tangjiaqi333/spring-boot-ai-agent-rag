package com.itheima.aiagent01.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    private String conversationId;
    private String role;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    private LocalDateTime createdAt;
    public ChatMessage() {
    }
    public ChatMessage(String conversationId, String role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public String getConversationId() {
        return conversationId;
    }
    public String getRole() {
        return role;
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
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
