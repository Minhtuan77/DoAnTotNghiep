package com.datn.backend.entity;

import com.datn.backend.entity.enums.DiscountType;
import com.datn.backend.entity.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "discount_type",
            nullable = false,
            length = 10
    )
    private DiscountType discountType;

    @Column(
            name = "discount_value",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal discountValue;

    @Column(
            name = "min_order_value",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(
            name = "max_discount_value",
            precision = 14,
            scale = 2
    )
    private BigDecimal maxDiscountValue;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(
            name = "used_count",
            nullable = false
    )
    @Builder.Default
    private Integer usedCount = 0;

    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDateTime startDate;

    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 15
    )
    @Builder.Default
    private VoucherStatus status = VoucherStatus.ACTIVE;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}
