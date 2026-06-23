package com.itheima.aiagent01.repository;

import com.itheima.aiagent01.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<ChatMessage> findAllByOrderByCreatedAtAsc();


}
