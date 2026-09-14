package com.datn.backend.entity;

import com.datn.backend.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(
            name = "sku",
            nullable = false,
            unique = true,
            length = 50
    )
    private String sku;

    @Column(
            name = "name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "slug",
            nullable = false,
            unique = true,
            length = 280
    )
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "short_desc", length = 500)
    private String shortDesc;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(
            name = "price",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal price;

    @Column(
            name = "sale_price",
            precision = 14,
            scale = 2
    )
    private BigDecimal salePrice;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Dữ liệu cache phục vụ hiển thị và sắp xếp sản phẩm.
     * Được cập nhật ở tầng Service.
     */
    @Column(
            name = "avg_rating",
            nullable = false,
            precision = 3,
            scale = 2
    )
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(
            name = "review_count",
            nullable = false
    )
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(
            name = "sold_count",
            nullable = false
    )
    @Builder.Default
    private Integer soldCount = 0;

    @Column(
            name = "view_count",
            nullable = false
    )
    @Builder.Default
    private Integer viewCount = 0;

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
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductSpecification> specifications = new ArrayList<>();
}