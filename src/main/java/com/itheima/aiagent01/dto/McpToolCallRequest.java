package com.itheima.aiagent01.dto;

public class McpToolCallRequest {

    private String input;

    public McpToolCallRequest() {
    }

    public McpToolCallRequest(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }
}