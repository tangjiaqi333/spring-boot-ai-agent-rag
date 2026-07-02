package com.itheima.aiagent01.dto;

public class McpToolResponse {

    private String name;
    private String description;

    public McpToolResponse() {
    }

    public McpToolResponse(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}