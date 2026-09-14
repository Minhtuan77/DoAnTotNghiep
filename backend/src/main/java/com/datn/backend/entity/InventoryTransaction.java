package com.datn.backend.entity;

import com.datn.backend.entity.enums.InventoryReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    /**
     * Số lượng thay đổi:
     *
     * Dương (+): Nhập kho
     * Âm (-): Xuất kho
     */
    @Column(
            name = "change_qty",
            nullable = false
    )
    private Integer changeQty;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reason",
            nullable = false,
            length = 20
    )
    private InventoryReason reason;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * User/Admin thực hiện thao tác.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
}
