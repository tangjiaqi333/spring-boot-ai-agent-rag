package com.itheima.aiagent01.dto;
//chatResponse:封装AI的回复
public class ChatResponse {
    private String reply;
    private String conversationId;
    public ChatResponse() {

    }
    public ChatResponse(String reply, String conversationId) {

        this.reply = reply;
        this.conversationId = conversationId;
    }
    public String getReply() {
        return reply;
    }
    public void setReply(String reply) {
        this.reply = reply;
    }
    public String getConversationId() {
        return conversationId;
    }
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

}
