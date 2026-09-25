package com.datn.backend.controller;

import com.datn.backend.dto.request.InventoryAdjustmentRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.InventoryResponse;
import com.datn.backend.dto.response.InventoryTransactionResponse;
import com.datn.backend.entity.enums.InventoryReason;
import com.datn.backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.datn.backend.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // =========================================================
    // GET INVENTORY BY PRODUCT
    // =========================================================

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<InventoryResponse>>
    getInventoryByProductId(
            @PathVariable Long productId
    ) {

        InventoryResponse response =
                inventoryService.getInventoryByProductId(
                        productId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy thông tin tồn kho thành công",
                        response
                )
        );
    }

    // =========================================================
    // GET ALL INVENTORIES
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>>
    getAllInventories(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<InventoryResponse> response =
                inventoryService.getAllInventories(
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Danh sách tồn kho",
                        response
                )
        );
    }

    // =========================================================
    // GET LOW STOCK
    // =========================================================

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>>
    getLowStockInventories(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<InventoryResponse> response =
                inventoryService.getLowStockInventories(
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Danh sách sản phẩm cảnh báo tồn kho thấp",
                        response
                )
        );
    }

    // =========================================================
    // ADJUST STOCK
    // =========================================================

    @PostMapping("/product/{productId}/adjust")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public ResponseEntity<ApiResponse<InventoryResponse>>
    adjustStock(
            @PathVariable Long productId,

            @Valid
            @RequestBody
            InventoryAdjustmentRequest request,

            @AuthenticationPrincipal
            CustomUserDetails userDetails
    ) {

        /*
         * Product ID lấy từ URL.
         *
         * Không sử dụng productId do client gửi
         * trong body để tránh lệch dữ liệu.
         */
        request.setProductId(productId);

        Long currentUserId = userDetails != null
                ? userDetails.getUserId()
                : null;

        InventoryResponse response =
                inventoryService.adjustStock(
                        request,
                        currentUserId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật tồn kho thành công",
                        response
                )
        );
    }

    // =========================================================
    // GET TRANSACTIONS BY PRODUCT
    // =========================================================

    @GetMapping("/product/{productId}/transactions")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<
            ApiResponse<Page<InventoryTransactionResponse>>
            >
    getTransactionsByProduct(
            @PathVariable Long productId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<InventoryTransactionResponse> response =
                inventoryService.getTransactions(
                        productId,
                        null,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lịch sử giao dịch tồn kho",
                        response
                )
        );
    }

    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<
            ApiResponse<Page<InventoryTransactionResponse>>
            >
    getTransactions(
            @RequestParam(required = false)
            Long productId,

            @RequestParam(required = false)
            InventoryReason reason,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<InventoryTransactionResponse> response =
                inventoryService.getTransactions(
                        productId,
                        reason,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy lịch sử giao dịch kho thành công",
                        response
                )
        );
    }
}