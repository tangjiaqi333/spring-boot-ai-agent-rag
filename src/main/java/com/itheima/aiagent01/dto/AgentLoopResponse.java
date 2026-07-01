package com.itheima.aiagent01.dto;

import java.util.ArrayList;
import java.util.List;

public class AgentLoopResponse {
    private String conversationId;
    private String finalAnswer;
    private List<AgentStepResponse> steps;
    public AgentLoopResponse() {
        this.steps = new ArrayList<>();
    }
    public AgentLoopResponse(String conversationId, String finalAnswer, List<AgentStepResponse> steps) {
        this.conversationId = conversationId;
        this.finalAnswer = finalAnswer;
        this.steps = steps;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }

    public List<AgentStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<AgentStepResponse> steps) {
        this.steps = steps;
    }
}
