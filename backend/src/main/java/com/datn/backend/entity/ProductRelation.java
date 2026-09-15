package com.datn.backend.entity;

import com.datn.backend.entity.enums.RelationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Quan hệ "sản phẩm tương tự" / "mua kèm" do AI Service tính định kỳ (batch job)
// rồi ghi đè bảng này để tra cứu nhanh theo product_id.
@Entity
@Table(
    name = "product_relations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_relation",
            columnNames = {
                "product_id",
                "related_product_id",
                "relation_type"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relation_id")
    private Long relationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "related_product_id", nullable = false)
    private Product relatedProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 15)
    private RelationType relationType;

    @Column(name = "score", nullable = false, precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal score = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}