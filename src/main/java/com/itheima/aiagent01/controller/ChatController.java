package com.itheima.aiagent01.controller;

import com.itheima.aiagent01.common.Result;
import com.itheima.aiagent01.dto.ChatMessageResponse;
import com.itheima.aiagent01.dto.ChatRequest;
import com.itheima.aiagent01.dto.ChatResponse;
import com.itheima.aiagent01.dto.ConversationResponse;
import com.itheima.aiagent01.service.AiChatService;
import com.itheima.aiagent01.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiChatService aiChatService;
    private final ConversationService conversationService;

    public ChatController(
            AiChatService aiChatService,
            ConversationService conversationService
    ) {
        this.aiChatService = aiChatService;
        this.conversationService = conversationService;
    }

    @PostMapping
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.chat(
                request.getConversationId(),
                request.getMessage()
        );

        return Result.success(response);
    }

    @DeleteMapping("/{conversationId}")
    public Result<String> clearConversation(@PathVariable String conversationId) {
        boolean removed = conversationService.clearConversation(conversationId);

        if (removed) {
            return Result.success("会话已经清除");
        }

        return Result.error(404, "会话不存在");
    }


    @GetMapping("/stream")
    public SseEmitter stream(
            @RequestParam(required = false) String conversationId,
            @RequestParam String message
    ) {
        SseEmitter emitter = new SseEmitter(60_000L);

        aiChatService.streamChat(message, conversationId, emitter);

        return emitter;
    }

    @GetMapping("/{conversationId}/messages")
    public Result<List<ChatMessageResponse>> getMessages(
            @PathVariable String conversationId
    ) {
        List<ChatMessageResponse> messages = conversationService.getMessages(conversationId);

        return Result.success(messages);
    }

    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversations() {
        List<ConversationResponse> conversations = conversationService.getConversations();

        return Result.success(conversations);
    }
}

