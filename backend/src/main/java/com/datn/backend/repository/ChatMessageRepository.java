package com.datn.backend.repository;

import com.datn.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop30ByConversation_ConversationIdOrderByCreatedAtDesc(Long conversationId);
    List<ChatMessage> findByConversation_ConversationIdOrderByCreatedAtAsc(Long conversationId);
}
