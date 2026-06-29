package com.itheima.aiagent01.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RagMessageResult {

    private List<Map<String, String>> messages;
    private List<SourceReferenceResponse> sources;

    public RagMessageResult() {
        this.messages = new ArrayList<>();
        this.sources = new ArrayList<>();
    }

    public RagMessageResult(List<Map<String, String>> messages, List<SourceReferenceResponse> sources) {
        this.messages = messages;
        this.sources = sources;
    }

    public List<Map<String, String>> getMessages() {
        return messages;
    }

    public void setMessages(List<Map<String, String>> messages) {
        this.messages = messages;
    }

    public List<SourceReferenceResponse> getSources() {
        return sources;
    }

    public void setSources(List<SourceReferenceResponse> sources) {
        this.sources = sources;
    }
}