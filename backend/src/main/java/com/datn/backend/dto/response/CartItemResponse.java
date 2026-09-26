package com.datn.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String sku;
    private String productName;
    private String imageUrl;
    private BigDecimal originalPrice;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Integer availableQuantity;
    private BigDecimal lineTotal;
}
