package com.itheima.aiagent01.dto;

public class AgentStepResponse {

    private Integer stepIndex;
    private String stepType;

    private String userMessage;
    private String modelDecision;

    private String toolName;
    private String toolInput;
    private String toolObservation;

    private String finalAnswer;

    public AgentStepResponse() {
    }

    public AgentStepResponse(
            Integer stepIndex,
            String stepType,
            String userMessage,
            String modelDecision,
            String toolName,
            String toolInput,
            String toolObservation,
            String finalAnswer
    ) {
        this.stepIndex = stepIndex;
        this.stepType = stepType;
        this.userMessage = userMessage;
        this.modelDecision = modelDecision;
        this.toolName = toolName;
        this.toolInput = toolInput;
        this.toolObservation = toolObservation;
        this.finalAnswer = finalAnswer;
    }

    public Integer getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(Integer stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getModelDecision() {
        return modelDecision;
    }

    public void setModelDecision(String modelDecision) {
        this.modelDecision = modelDecision;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolInput() {
        return toolInput;
    }

    public void setToolInput(String toolInput) {
        this.toolInput = toolInput;
    }

    public String getToolObservation() {
        return toolObservation;
    }

    public void setToolObservation(String toolObservation) {
        this.toolObservation = toolObservation;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }
}