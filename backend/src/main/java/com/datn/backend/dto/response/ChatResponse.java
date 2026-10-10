package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ChatConversationStatus;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private Long conversationId;
    private ChatConversationStatus status;
    private ChatMessageResponse reply;
}
