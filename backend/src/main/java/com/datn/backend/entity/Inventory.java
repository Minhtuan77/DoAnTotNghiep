package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    /**
     * Khóa chính của Inventory đồng thời là product_id.
     */
    @Id
    @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(
            name = "quantity_on_hand",
            nullable = false
    )
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(
            name = "quantity_reserved",
            nullable = false
    )
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(
            name = "low_stock_threshold",
            nullable = false
    )
    @Builder.Default
    private Integer lowStockThreshold = 5;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}
