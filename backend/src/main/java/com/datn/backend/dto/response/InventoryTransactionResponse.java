package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.InventoryReason;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryTransactionResponse {
    private Long transactionId;
    private Long productId;
    private String productName;
    private Integer changeQty;
    private InventoryReason reason;
    private Long referenceId;
    private String referenceType;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}