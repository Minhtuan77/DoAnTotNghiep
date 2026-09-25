package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private Double ratingAverage;
    private Integer reviewCount;
    private ProductStatus status;

    private CategoryResponse category;
    private BrandResponse brand;

    private List<ProductImageResponse> images;
    private List<ProductSpecificationResponse> specifications;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
