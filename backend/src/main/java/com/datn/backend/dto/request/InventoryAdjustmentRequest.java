package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.InventoryReason;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng thay đổi không được để trống")
    private Integer changeQty; // Dương (+) nếu nhập/tăng, Âm (-) nếu giảm/xuất

    @NotNull(message = "Lý do thay đổi không được để trống")
    private InventoryReason reason; // IMPORT, ADJUSTMENT,...

    private String referenceType; // e.g., "PURCHASE_ORDER", "MANUAL_CHECK"
    private Long referenceId;
}