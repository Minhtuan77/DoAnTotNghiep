package com.datn.backend.service;

import com.datn.backend.dto.request.ChatRequest;
import com.datn.backend.dto.response.ChatConversationResponse;
import com.datn.backend.dto.response.ChatResponse;
import java.util.List;

public interface ChatbotService {
    ChatResponse send(Long userId, ChatRequest request);
    ChatConversationResponse getConversation(Long userId, String sessionId, Long conversationId);
    List<ChatConversationResponse> list(Long userId, String sessionId);
    ChatConversationResponse close(Long userId, String sessionId, Long conversationId);
}
