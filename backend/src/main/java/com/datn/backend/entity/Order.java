package com.datn.backend.entity;

import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(
            name = "order_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "recipient_name",
            nullable = false,
            length = 150
    )
    private String recipientName;

    @Column(
            name = "recipient_phone",
            nullable = false,
            length = 20
    )
    private String recipientPhone;

    @Column(
            name = "shipping_address",
            nullable = false,
            length = 500
    )
    private String shippingAddress;

    @Column(name = "note", length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_method",
            nullable = false,
            length = 10
    )
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 25
    )
    @Builder.Default
    private OrderStatus status =
            OrderStatus.PENDING_CONFIRMATION;

    @Column(
            name = "subtotal_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal subtotalAmount;

    @Column(
            name = "shipping_fee",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderStatusHistory> statusHistories =
            new ArrayList<>();
}
