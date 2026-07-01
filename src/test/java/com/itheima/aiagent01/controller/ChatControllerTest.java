package com.itheima.aiagent01.controller;

import com.itheima.aiagent01.dto.ChatResponse;
import com.itheima.aiagent01.service.AiChatService;
import com.itheima.aiagent01.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    @MockitoBean
    private ConversationService conversationService;

    @Test
    void shouldReturnChatResponse() throws Exception {
        ChatResponse chatResponse = new ChatResponse(
                "RAG 需要切片是因为长文档太长，需要拆成多个 chunk 进行检索。",
                "conv-1"
        );

        when(aiChatService.chat(any(), anyString()))
                .thenReturn(chatResponse);

        mockMvc.perform(post("/api/chat")
                        .contentType("application/json")
                        .content("""
                                {
                                  "message": "RAG 为什么需要切片？"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content").value("RAG 需要切片是因为长文档太长，需要拆成多个 chunk 进行检索。"))
                .andExpect(jsonPath("$.data.conversationId").value("conv-1"));
    }
}