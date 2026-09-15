package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Column(
            name = "from_status",
            length = 30
    )
    private String fromStatus;

    @Column(
            name = "to_status",
            nullable = false,
            length = 30
    )
    private String toStatus;

    /**
     * User/Admin thực hiện thay đổi.
     * NULL nếu hệ thống tự động thay đổi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "note", length = 255)
    private String note;

    @CreationTimestamp
    @Column(
            name = "changed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime changedAt;
}
