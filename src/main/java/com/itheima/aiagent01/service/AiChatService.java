package com.itheima.aiagent01.service;

import com.itheima.aiagent01.client.DeepSeekClient;
import com.itheima.aiagent01.dto.ChatResponse;
import com.itheima.aiagent01.dto.RagAnswerResult;
import com.itheima.aiagent01.dto.RagMessageResult;
import com.itheima.aiagent01.dto.SourceReferenceResponse;
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

        RagAnswerResult  ragAnswerResult= ragService.answerIfKnowledgeMatched(message);

        if (ragAnswerResult != null) {
            System.out.println("RAG 命中，使用知识库回答");
            conversationService.saveMessage(conversationId, "assistant", ragAnswerResult.getAnswer());

            return new ChatResponse(
                    ragAnswerResult.getAnswer(),
                    conversationId,
                    true,
                    ragAnswerResult.getSources());
        }
        System.out.println("RAG 未命中，使用 DeepSeek 模型回答");

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

                RagMessageResult ragMessageResult =
                        ragService.buildRagMessagesIfKnowledgeMatched(message);

                List<Map<String, String>> messages;
                boolean ragUsed = false;
                List<SourceReferenceResponse> sources = new ArrayList<>();

                if (ragMessageResult != null) {
                    System.out.println("Stream RAG 命中，使用知识库回答");

                    ragUsed = true;
                    messages = ragMessageResult.getMessages();
                    sources = ragMessageResult.getSources();
                } else {
                    System.out.println("Stream RAG 未命中，走普通 DeepSeek 兜底");

                    messages = conversationService.loadHistoryFromDb(finalConversationId);
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
                        .name("ragUsed")
                        .data(ragUsed));
                if (ragUsed) {
                    emitter.send(SseEmitter.event()
                            .name("sources")
                            .data(sources));
                }

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
