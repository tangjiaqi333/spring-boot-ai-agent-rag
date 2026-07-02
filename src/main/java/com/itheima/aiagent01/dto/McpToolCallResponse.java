package com.itheima.aiagent01.dto;

public class McpToolCallResponse {

    private String toolName;
    private String input;
    private String result;

    public McpToolCallResponse() {
    }

    public McpToolCallResponse(String toolName, String input, String result) {
        this.toolName = toolName;
        this.input = input;
        this.result = result;
    }

    public String getToolName() {
        return toolName;
    }

    public String getInput() {
        return input;
    }

    public String getResult() {
        return result;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setResult(String result) {
        this.result = result;
    }
}