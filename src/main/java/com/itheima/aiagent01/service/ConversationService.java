package com.itheima.aiagent01.service;

import com.itheima.aiagent01.dto.ChatMessageResponse;
import com.itheima.aiagent01.dto.ConversationResponse;
import com.itheima.aiagent01.entity.ChatMessage;
import com.itheima.aiagent01.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationService {

    private final ChatMessageRepository chatMessageRepository;

    public ConversationService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }
    public void saveMessage(String conversationId, String role, String content) {
        ChatMessage chatMessage = new ChatMessage(conversationId, role, content);
        chatMessageRepository.save(chatMessage);
    }

        public List<Map<String, String>> loadHistoryFromDb(String conversationId) {
        List<ChatMessage> chatMessages =
                chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<Map<String, String>> history = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages) {
            history.add(Map.of(
                    "role" , chatMessage.getRole(),
                    "content", chatMessage.getContent()
            ));
        }
        if (history.size() > 10) {
            return new ArrayList<>(history.subList(history.size() - 10 , history.size() ));

        }
        return history;
        }

        public List<ChatMessageResponse> getMessages(String conversationId) {
        List<ChatMessage> chatMessages =
                chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<ChatMessageResponse> responses = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages) {
            responses.add(new ChatMessageResponse(
                    chatMessage.getRole(),
                    chatMessage.getContent(),
                    chatMessage.getCreatedAt()
            ));
        }
        return responses;
        }

        public List<ConversationResponse> getConversations() {
        List<ChatMessage> allMessages =
                chatMessageRepository.findAllByOrderByCreatedAtAsc();
        Map<String, ConversationResponse> conversationMap = new LinkedHashMap<>();

        for (ChatMessage message : allMessages) {
            String conversationId = message.getConversationId();
            if (!conversationMap.containsKey(conversationId)) {
                conversationMap.put(
                        conversationId,
                        new ConversationResponse(
                                conversationId,
                                message.getCreatedAt())
                );
            }
        }

        return new ArrayList<>(conversationMap.values());
    }

    public boolean clearConversation(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return false;
        }
        List<ChatMessage> messages =
                chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (messages.isEmpty()) {
            return false;
        }
        chatMessageRepository.deleteAll(messages);
        return true;
    }
}


