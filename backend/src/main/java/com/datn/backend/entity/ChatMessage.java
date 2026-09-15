package com.datn.backend.entity;

import com.datn.backend.entity.enums.ChatSenderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 10)
    private ChatSenderType senderType;

    @Lob
    @Column(name = "message_text", nullable = false)
    private String messageText;

    // Ghi lại ngữ cảnh sản phẩm/đơn hàng... mà chatbot đã dùng để trả lời
    // phục vụ audit và kiểm soát phạm vi dữ liệu của bot.
    @Column(name = "referenced_entity_type", length = 30)
    private String referencedEntityType;

    @Column(name = "referenced_entity_id")
    private Long referencedEntityId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}