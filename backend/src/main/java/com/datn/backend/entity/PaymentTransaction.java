package com.datn.backend.entity;

import com.datn.backend.entity.enums.PaymentTransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    /**
     * Ví dụ:
     * VNPAY
     * MOMO
     * ZALOPAY
     * STRIPE
     */
    @Column(
            name = "provider",
            nullable = false,
            length = 50
    )
    private String provider;

    @Column(
            name = "provider_txn_code",
            length = 100
    )
    private String providerTxnCode;

    @Column(
            name = "amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 15
    )
    @Builder.Default
    private PaymentTransactionStatus status =
            PaymentTransactionStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /**
     * Lưu response JSON nguyên bản
     * từ payment gateway để audit/đối soát.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "raw_response",
            columnDefinition = "json"
    )
    private String rawResponse;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}
