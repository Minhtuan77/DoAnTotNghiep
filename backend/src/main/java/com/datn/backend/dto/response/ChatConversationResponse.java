package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ChatConversationStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatConversationResponse {
    private Long conversationId;
    private ChatConversationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private List<ChatMessageResponse> messages;
}
