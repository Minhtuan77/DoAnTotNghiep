package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    /**
     * Snapshot tên sản phẩm tại thời điểm mua.
     */
    @Column(
            name = "product_name_snapshot",
            nullable = false,
            length = 255
    )
    private String productNameSnapshot;

    /**
     * Snapshot giá sản phẩm tại thời điểm mua.
     */
    @Column(
            name = "unit_price_snapshot",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal unitPriceSnapshot;

    @Column(
            name = "quantity",
            nullable = false
    )
    private Integer quantity;

    @Column(
            name = "line_subtotal",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal lineSubtotal;
}
