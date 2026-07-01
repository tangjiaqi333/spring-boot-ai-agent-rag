package com.itheima.aiagent01.dto;

public class AgentAction {
    private String action;
    private String toolName;
    private String toolInput;
    private String answer;

    public AgentAction() {
    }

    public AgentAction(String action, String toolName, String toolInput, String answer) {
        this.action = action;
        this.toolName = toolName;
        this.toolInput = toolInput;
        this.answer = answer;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
