package com.itheima.aiagent01.dto;

import jakarta.validation.constraints.NotBlank;

//chatrequest = 用户发送的请求
public class ChatRequest {
    @NotBlank(message = "消息不能为空")
    private String message;

    private String conversationId;

    public ChatRequest() {

    }
    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
