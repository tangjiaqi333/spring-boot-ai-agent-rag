package com.itheima.aiagent01.controller;

import com.itheima.aiagent01.dto.McpToolResponse;
import com.itheima.aiagent01.service.AgentLoopService;
import com.itheima.aiagent01.service.AiChatService;
import com.itheima.aiagent01.service.ConversationService;
import com.itheima.aiagent01.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = McpController.class)
class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ToolRegistry toolRegistry;

    @MockitoBean
    private AiChatService aiChatService;

    @MockitoBean
    private ConversationService conversationService;

    @MockitoBean
    private AgentLoopService agentLoopService;

    @Test
    void shouldReturnToolList() throws Exception {
        List<McpToolResponse> tools = List.of(
                new McpToolResponse("calculator", "执行数学计算"),
                new McpToolResponse("knowledge_search", "从知识库中检索相关内容"),
                new McpToolResponse("document_list", "查询当前知识库文档列表")
        );

        when(toolRegistry.listTools()).thenReturn(tools);

        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("calculator"))
                .andExpect(jsonPath("$.data[0].description").value("执行数学计算"))
                .andExpect(jsonPath("$.data[1].name").value("knowledge_search"))
                .andExpect(jsonPath("$.data[2].name").value("document_list"));
    }

    @Test
    void shouldCallCalculatorTool() throws Exception {
        when(toolRegistry.execute("calculator", "888*55877"))
                .thenReturn("49618776");

        mockMvc.perform(post("/api/mcp/tools/calculator/call")
                        .contentType("application/json")
                        .content("""
                                {
                                  "input": "888*55877"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.toolName").value("calculator"))
                .andExpect(jsonPath("$.data.input").value("888*55877"))
                .andExpect(jsonPath("$.data.result").value("49618776"));
    }

    @Test
    void shouldCallKnowledgeSearchTool() throws Exception {
        when(toolRegistry.execute("knowledge_search", "RAG 为什么需要切片？"))
                .thenReturn("知识库检索结果：RAG 需要切片是因为长文档太长，需要拆成多个 chunk。");

        mockMvc.perform(post("/api/mcp/tools/knowledge_search/call")
                        .contentType("application/json")
                        .content("""
                                {
                                  "input": "RAG 为什么需要切片？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.toolName").value("knowledge_search"))
                .andExpect(jsonPath("$.data.input").value("RAG 为什么需要切片？"))
                .andExpect(jsonPath("$.data.result").value("知识库检索结果：RAG 需要切片是因为长文档太长，需要拆成多个 chunk。"));
    }

    @Test
    void shouldReturnUnknownToolMessage() throws Exception {
        when(toolRegistry.execute("not_exist", "hello"))
                .thenReturn("未知工具：not_exist");

        mockMvc.perform(post("/api/mcp/tools/not_exist/call")
                        .contentType("application/json")
                        .content("""
                                {
                                  "input": "hello"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.toolName").value("not_exist"))
                .andExpect(jsonPath("$.data.input").value("hello"))
                .andExpect(jsonPath("$.data.result").value("未知工具：not_exist"));
    }

    @Test
    void shouldCallToolWithoutBody() throws Exception {
        when(toolRegistry.execute("document_list", null))
                .thenReturn("当前知识库文档列表：agent_chunk_test.txt");

        mockMvc.perform(post("/api/mcp/tools/document_list/call"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.toolName").value("document_list"))
                .andExpect(jsonPath("$.data.input").doesNotExist())
                .andExpect(jsonPath("$.data.result").value("当前知识库文档列表：agent_chunk_test.txt"));
    }
}