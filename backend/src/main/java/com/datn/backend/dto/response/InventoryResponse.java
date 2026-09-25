package com.datn.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer availableQuantity; // quantityOnHand - quantityReserved
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private LocalDateTime updatedAt;
}