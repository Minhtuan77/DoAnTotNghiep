package com.datn.backend.service;

import com.datn.backend.dto.request.InventoryAdjustmentRequest;
import com.datn.backend.dto.response.InventoryResponse;
import com.datn.backend.dto.response.InventoryTransactionResponse;
import com.datn.backend.entity.enums.InventoryReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    /**
     * Lấy thông tin tồn kho của một sản phẩm.
     */
    InventoryResponse getInventoryByProductId(Long productId);

    /**
     * Lấy toàn bộ danh sách tồn kho có phân trang.
     */
    Page<InventoryResponse> getAllInventories(Pageable pageable);

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp.
     */
    Page<InventoryResponse> getLowStockInventories(Pageable pageable);

    /**
     * Điều chỉnh số lượng tồn kho.
     */
    InventoryResponse adjustStock(
            InventoryAdjustmentRequest request,
            Long userId
    );

    /**
     * Xử lý biến động tồn kho liên quan đến đơn hàng.
     */
    void processOrderStock(
            Long productId,
            Integer quantity,
            InventoryReason reason,
            Long orderId,
            Long userId
    );

    /**
     * Lấy lịch sử giao dịch kho.
     *
     * @param productId lọc theo sản phẩm, có thể null
     * @param reason lọc theo lý do giao dịch, có thể null
     * @param pageable thông tin phân trang
     */
    Page<InventoryTransactionResponse> getTransactions(
            Long productId,
            InventoryReason reason,
            Pageable pageable
    );
}