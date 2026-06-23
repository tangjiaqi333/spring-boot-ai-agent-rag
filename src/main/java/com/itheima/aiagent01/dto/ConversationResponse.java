package com.itheima.aiagent01.dto;

import java.time.LocalDateTime;

public class ConversationResponse {
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LocalDateTime getLatestMessageTime() {
        return latestMessageTime;
    }

    public void setLatestMessageTime(LocalDateTime latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
    }

    private String conversationId;
    private LocalDateTime latestMessageTime;

    public ConversationResponse() {
    }

    public ConversationResponse( String conversationId,LocalDateTime latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
        this.conversationId = conversationId;
    }
}
