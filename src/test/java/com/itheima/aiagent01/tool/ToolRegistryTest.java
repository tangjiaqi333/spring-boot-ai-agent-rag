package com.itheima.aiagent01.tool;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ToolRegistryTest {

    @Test
    void shouldExecuteRegisteredTool() {
        AgentTool echoTool = new AgentTool() {
            @Override
            public String getName() {
                return "echo";
            }

            @Override
            public String getDescription() {
                return "回显输入内容";
            }

            @Override
            public String execute(String input) {
                return "echo: " + input;
            }
        };

        ToolRegistry toolRegistry = new ToolRegistry(List.of(echoTool));

        String result = toolRegistry.execute("echo", "hello");

        assertEquals("echo: hello", result);
    }

    @Test
    void shouldReturnUnknownToolMessageWhenToolNotFound() {
        ToolRegistry toolRegistry = new ToolRegistry(List.of());

        String result = toolRegistry.execute("not_exist", "hello");

        assertEquals("未知工具：not_exist", result);
    }

    @Test
    void shouldReturnErrorWhenToolNameIsBlank() {
        ToolRegistry toolRegistry = new ToolRegistry(List.of());

        String result = toolRegistry.execute("", "hello");

        assertEquals("工具名不能为空", result);
    }

    @Test
    void shouldReturnToolDescriptions() {
        AgentTool calculatorTool = new AgentTool() {
            @Override
            public String getName() {
                return "calculator";
            }

            @Override
            public String getDescription() {
                return "执行数学计算";
            }

            @Override
            public String execute(String input) {
                return "ok";
            }
        };

        AgentTool timeTool = new AgentTool() {
            @Override
            public String getName() {
                return "current_time";
            }

            @Override
            public String getDescription() {
                return "获取当前时间";
            }

            @Override
            public String execute(String input) {
                return "now";
            }
        };

        ToolRegistry toolRegistry = new ToolRegistry(List.of(calculatorTool, timeTool));

        String descriptions = toolRegistry.getToolDescriptions();

        assertTrue(descriptions.contains("calculator"));
        assertTrue(descriptions.contains("执行数学计算"));
        assertTrue(descriptions.contains("current_time"));
        assertTrue(descriptions.contains("获取当前时间"));
    }
}