package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ChatSenderType;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private ChatSenderType senderType;
    private String messageText;
    private String referencedEntityType;
    private Long referencedEntityId;
    private LocalDateTime createdAt;
}
