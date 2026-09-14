package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Column(
            name = "image_url",
            nullable = false,
            length = 500
    )
    private String imageUrl;

    @Column(
            name = "is_primary",
            nullable = false
    )
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(
            name = "sort_order",
            nullable = false
    )
    @Builder.Default
    private Short sortOrder = 0;
}