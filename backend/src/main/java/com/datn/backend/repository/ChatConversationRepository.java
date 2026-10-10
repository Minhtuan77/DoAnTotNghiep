package com.datn.backend.repository;

import com.datn.backend.entity.ChatConversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<ChatConversation> findByConversationIdAndUser_UserId(Long conversationId, Long userId);
    Optional<ChatConversation> findByConversationIdAndSessionId(Long conversationId, String sessionId);
    List<ChatConversation> findTop20ByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<ChatConversation> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
