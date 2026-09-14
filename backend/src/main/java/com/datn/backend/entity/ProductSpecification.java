package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSpecification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    private Long specId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Column(
            name = "spec_name",
            nullable = false,
            length = 100
    )
    private String specName;

    @Column(
            name = "spec_value",
            nullable = false,
            length = 255
    )
    private String specValue;

    @Column(
            name = "sort_order",
            nullable = false
    )
    @Builder.Default
    private Short sortOrder = 0;
}