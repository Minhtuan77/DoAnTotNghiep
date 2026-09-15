package com.datn.backend.entity;

import com.datn.backend.entity.enums.BehaviorActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// Bảng ghi log hành vi phục vụ AI (gợi ý sản phẩm) — insert-only, khối lượng ghi lớn.
// Cân nhắc PARTITION BY RANGE(created_at) theo tháng khi dữ liệu tăng trưởng.
@Entity
@Table(name = "user_behavior_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBehaviorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    // NULL nếu khách chưa đăng nhập (theo dõi bằng session_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 15)
    private BehaviorActionType actionType;

    @Column(name = "search_keyword", length = 255)
    private String searchKeyword;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
