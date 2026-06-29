package com.itheima.aiagent01.dto;

import java.util.ArrayList;
import java.util.List;

//chatResponse:封装AI的回复
public class ChatResponse {
    private String content;
    private String conversationId;
    private Boolean ragUsed;
    private List<SourceReferenceResponse> sources;

    public ChatResponse() {
    }

    public ChatResponse(String content, String conversationId) {

        this.content = content;
        this.conversationId = conversationId;
        this.ragUsed = false;
        this.sources = new ArrayList<>();
    }

    public ChatResponse(String content, String conversationId, Boolean ragUsed, List<SourceReferenceResponse> sources) {
        this.content = content;
        this.conversationId = conversationId;
        this.ragUsed = ragUsed;
        this.sources = sources;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Boolean getRagUsed() {
        return ragUsed;
    }

    public void setRagUsed(Boolean ragUsed) {
        this.ragUsed = ragUsed;
    }

    public List<SourceReferenceResponse> getSources() {
        return sources;
    }

    public void setSources(List<SourceReferenceResponse> sources) {
        this.sources = sources;
    }





}
