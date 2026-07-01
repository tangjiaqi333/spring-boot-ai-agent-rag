package com.itheima.aiagent01.service;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.AgentLoopResponse;
import com.itheima.aiagent01.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentLoopServiceTest {

    private final ToolCallingService toolCallingService = mock(ToolCallingService.class);
    private final DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final AgentActionParser agentActionParser = new AgentActionParser();
    private final ToolRegistry toolRegistry = mock(ToolRegistry.class);

    private AgentLoopService createService() {
        AgentLoopService service = new AgentLoopService(
                toolCallingService,
                deepSeekClient,
                conversationService,
                toolRegistry,
                agentActionParser

        );

        ReflectionTestUtils.setField(service, "systemPrompt", "你是一个测试助手");

        return service;
    }

    @Test
    void shouldCallToolAndReturnFinalAnswer() {
        AgentLoopService service = createService();

        when(deepSeekClient.chat(anyList()))
                .thenReturn("""
                        {
                          "action": "tool",
                          "toolName": "calculator",
                          "toolInput": "12345*678",
                          "answer": null
                        }
                        """)
                .thenReturn("""
                        {
                          "action": "final",
                          "toolName": null,
                          "toolInput": null,
                          "answer": "12345 乘以 678 等于 8369910"
                        }
                        """);

        when(toolRegistry.execute("calculator", "12345*678"))
                .thenReturn("8369910");

        AgentLoopResponse response =
                service.runV2(null, "12345 乘以 678 等于多少？");

        assertNotNull(response.getConversationId());
        assertEquals("12345 乘以 678 等于 8369910", response.getFinalAnswer());
        assertEquals(4, response.getSteps().size());

        assertEquals("THINK", response.getSteps().get(0).getStepType());
        assertEquals("TOOL_OBSERVATION", response.getSteps().get(1).getStepType());
        assertEquals("THINK", response.getSteps().get(2).getStepType());
        assertEquals("FINAL_ANSWER", response.getSteps().get(3).getStepType());

        verify(toolRegistry).execute("calculator", "12345*678");
        verify(conversationService).saveMessage(anyString(), eq("user"), anyString());
        verify(conversationService).saveMessage(anyString(), eq("assistant"), anyString());
    }

    @Test
    void shouldReturnFinalDirectlyWhenModelReturnsFinalAction() {
        AgentLoopService service = createService();

        when(deepSeekClient.chat(anyList()))
                .thenReturn("""
                        {
                          "action": "final",
                          "toolName": null,
                          "toolInput": null,
                          "answer": "RAG 是检索增强生成"
                        }
                        """);

        AgentLoopResponse response =
                service.runV2("conv-1", "什么是 RAG？");

        assertEquals("conv-1", response.getConversationId());
        assertEquals("RAG 是检索增强生成", response.getFinalAnswer());
        assertEquals(2, response.getSteps().size());

        verify(toolRegistry, never()).execute(anyString(), anyString());
    }

    @Test
    void shouldFallbackWhenModelReturnsInvalidJson() {
        AgentLoopService service = createService();

        when(deepSeekClient.chat(anyList()))
                .thenReturn("这是一个非 JSON 的回答");

        AgentLoopResponse response =
                service.runV2(null, "随便问一句");

        assertEquals("这是一个非 JSON 的回答", response.getFinalAnswer());
        assertFalse(response.getSteps().isEmpty());
    }

    @Test
    void shouldStopWhenMaxStepsReached() {
        AgentLoopService service = createService();

        when(deepSeekClient.chat(anyList()))
                .thenReturn("""
                        {
                          "action": "tool",
                          "toolName": "calculator",
                          "toolInput": "1+1",
                          "answer": null
                        }
                        """);

        when(toolRegistry.execute("calculator", "1+1"))
                .thenReturn("2");

        AgentLoopResponse response =
                service.runV2(null, "一直调用工具");

        assertEquals("Agent 达到最大执行步数，任务未完成", response.getFinalAnswer());

        verify(toolRegistry, times(3)).execute("calculator", "1+1");
    }
}