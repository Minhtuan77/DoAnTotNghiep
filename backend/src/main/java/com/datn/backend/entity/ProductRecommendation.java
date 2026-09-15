package com.datn.backend.entity;

import com.datn.backend.entity.enums.RecommendationPlacement;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Kết quả gợi ý cá nhân hoá đã được AI Service tính sẵn (cache)
// theo từng user/vị trí hiển thị.
@Entity
@Table(name = "product_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    // NULL = gợi ý chung cho khách mới/chưa đăng nhập
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "placement", nullable = false, length = 20)
    private RecommendationPlacement placement;

    @Column(name = "score", nullable = false, precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal score = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}