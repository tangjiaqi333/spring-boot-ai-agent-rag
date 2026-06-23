package com.itheima.aiagent01.service;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.ChatResponse;
import com.itheima.aiagent01.rag.RagService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiChatService {

    @Value("${ai.system-prompt}")
    private String systemPrompt;

    private final RagService ragService;
    private final ConversationService conversationService;
    private final DeepSeekClient deepSeekClient;
    private final ToolCallingService toolCallingService;

    public AiChatService(
            RagService ragService,
            ConversationService conversationService,
            DeepSeekClient deepSeekClient,
            ToolCallingService toolCallingService
    ) {
        this.ragService = ragService;
        this.conversationService = conversationService;
        this.deepSeekClient = deepSeekClient;
        this.toolCallingService = toolCallingService;
    }

    public ChatResponse chat(String conversationId, String message) {
        String finalConversationId = StringUtils.hasText(conversationId)
                ? conversationId
                : UUID.randomUUID().toString();

        List<Map<String, String>> history =
                conversationService.loadHistoryFromDb(finalConversationId);

        String toolReply = toolCallingService.handleToolCallIfNeeded(message);

        if (StringUtils.hasText(toolReply)) {
            conversationService.saveMessage(finalConversationId, "user", message);
            conversationService.saveMessage(finalConversationId, "assistant", toolReply);

            return new ChatResponse(toolReply, finalConversationId);
        }

        String ragReply = ragService.answerIfKnowledgeMatched(message);

        if (StringUtils.hasText(ragReply)) {
            conversationService.saveMessage(finalConversationId, "user", message);
            conversationService.saveMessage(finalConversationId, "assistant", ragReply);

            return new ChatResponse(ragReply, finalConversationId);
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(history);
        messages.add(Map.of("role", "user", "content", message));

        String reply = deepSeekClient.chat(messages);

        conversationService.saveMessage(finalConversationId, "user", message);
        conversationService.saveMessage(finalConversationId, "assistant", reply);

        return new ChatResponse(reply, finalConversationId);
    }

    public void streamChat(String message, String conversationId, SseEmitter emitter) {
        String finalConversationId = StringUtils.hasText(conversationId)
                ? conversationId
                : UUID.randomUUID().toString();

        new Thread(() -> {
            StringBuilder fullReply = new StringBuilder();

            try {
                emitter.send(SseEmitter.event()
                        .name("conversationId")
                        .data(finalConversationId));

                List<Map<String, String>> history =
                        conversationService.loadHistoryFromDb(finalConversationId);

                String toolReply = toolCallingService.handleToolCallIfNeeded(message);

                if (StringUtils.hasText(toolReply)) {
                    conversationService.saveMessage(finalConversationId, "user", message);
                    conversationService.saveMessage(finalConversationId, "assistant", toolReply);

                    sendTextBySse(toolReply, emitter);

                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data("[DONE]"));

                    emitter.complete();
                    return;
                }

                List<Map<String, String>> messages = new ArrayList<>();

                List<Map<String, String>> ragMessages =
                        ragService.buildRagMessagesIfKnowledgeMatched(message);

                if (ragMessages != null) {
                    messages.addAll(ragMessages);
                } else {
                    messages.add(Map.of("role", "system", "content", systemPrompt));
                    messages.addAll(history);
                    messages.add(Map.of("role", "user", "content", message));
                }

                deepSeekClient.streamChat(messages, content -> {
                    try {
                        fullReply.append(content);

                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(content));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                conversationService.saveMessage(finalConversationId, "user", message);
                conversationService.saveMessage(finalConversationId, "assistant", fullReply.toString());

                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("[DONE]"));

                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("流式输出失败：" + e.getMessage()));
                } catch (Exception ignored) {
                }

                emitter.complete();
            }
        }).start();
    }

    private void sendTextBySse(String text, SseEmitter emitter) throws Exception {
        for (int i = 0; i < text.length(); i++) {
            String word = String.valueOf(text.charAt(i));

            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(word));

            Thread.sleep(50);
        }
    }
}
