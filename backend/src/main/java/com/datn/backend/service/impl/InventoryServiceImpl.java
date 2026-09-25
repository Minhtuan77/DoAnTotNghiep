package com.datn.backend.service.impl;

import com.datn.backend.dto.request.InventoryAdjustmentRequest;
import com.datn.backend.dto.response.InventoryResponse;
import com.datn.backend.dto.response.InventoryTransactionResponse;
import com.datn.backend.entity.Inventory;
import com.datn.backend.entity.InventoryTransaction;
import com.datn.backend.entity.Product;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.InventoryReason;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.InventoryRepository;
import com.datn.backend.repository.InventoryTransactionRepository;
import com.datn.backend.repository.ProductRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // =========================================================
    // GET INVENTORY BY PRODUCT ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProduct_ProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy thông tin kho cho sản phẩm ID: "
                                        + productId
                        )
                );

        return mapToInventoryResponse(inventory);
    }

    // =========================================================
    // GET ALL INVENTORIES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getAllInventories(
            Pageable pageable
    ) {

        return inventoryRepository
                .findAll(pageable)
                .map(this::mapToInventoryResponse);
    }

    // =========================================================
    // GET LOW STOCK INVENTORIES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockInventories(
            Pageable pageable
    ) {

        return inventoryRepository
                .findLowStockProducts(pageable)
                .map(this::mapToInventoryResponse);
    }

    // =========================================================
    // ADJUST STOCK
    // =========================================================

    @Override
    @Transactional
    public InventoryResponse adjustStock(
            InventoryAdjustmentRequest request,
            Long userId
    ) {

        // -----------------------------------------------------
        // Tìm sản phẩm
        // -----------------------------------------------------

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy sản phẩm ID: "
                                        + request.getProductId()
                        )
                );

        // -----------------------------------------------------
        // Tìm inventory
        // -----------------------------------------------------

        Inventory inventory = inventoryRepository
                .findByProduct_ProductId(request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy thông tin kho cho sản phẩm ID: "
                                        + request.getProductId()
                        )
                );

        // -----------------------------------------------------
        // Tìm user thực hiện thao tác
        // -----------------------------------------------------

        User user = null;

        if (userId != null) {
            user = userRepository
                    .findById(userId)
                    .orElse(null);
        }

        // -----------------------------------------------------
        // Tính tồn kho mới
        // -----------------------------------------------------

        int currentQuantity =
                inventory.getQuantityOnHand();

        int changeQuantity =
                request.getChangeQty();

        int newQuantity =
                currentQuantity + changeQuantity;

        // Không cho phép tồn kho âm
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Số lượng tồn kho không đủ để thực hiện điều chỉnh"
            );
        }

        // -----------------------------------------------------
        // Không cho phép tồn khả dụng âm
        // -----------------------------------------------------

        int quantityReserved =
                inventory.getQuantityReserved();

        if (newQuantity < quantityReserved) {
            throw new IllegalArgumentException(
                    "Số lượng tồn kho mới không thể nhỏ hơn số lượng đang được giữ"
            );
        }

        // -----------------------------------------------------
        // Cập nhật inventory
        // -----------------------------------------------------

        inventory.setQuantityOnHand(newQuantity);

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        // -----------------------------------------------------
        // Ghi lịch sử giao dịch
        // -----------------------------------------------------

        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .product(product)
                        .changeQty(changeQuantity)
                        .reason(request.getReason())
                        .referenceType(request.getReferenceType())
                        .referenceId(request.getReferenceId())
                        .createdBy(user)
                        .build();

        transactionRepository.save(transaction);

        // -----------------------------------------------------
        // Return
        // -----------------------------------------------------

        return mapToInventoryResponse(savedInventory);
    }

    // =========================================================
    // PROCESS ORDER STOCK
    // =========================================================

    @Override
    @Transactional
    public void processOrderStock(
            Long productId,
            Integer quantity,
            InventoryReason reason,
            Long orderId,
            Long userId
    ) {

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID không được để trống"
            );
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Số lượng sản phẩm phải lớn hơn 0"
            );
        }

        if (reason == null) {
            throw new IllegalArgumentException(
                    "Lý do giao dịch không được để trống"
            );
        }

        InventoryAdjustmentRequest request =
                new InventoryAdjustmentRequest();

        request.setProductId(productId);
        request.setReferenceType("ORDER");
        request.setReferenceId(orderId);
        request.setReason(reason);

        // -----------------------------------------------------
        // Đặt hàng -> giảm tồn kho
        // -----------------------------------------------------

        if (reason == InventoryReason.ORDER_PLACED) {

            request.setChangeQty(-quantity);
        }

        // -----------------------------------------------------
        // Hủy / hoàn trả -> tăng tồn kho
        // -----------------------------------------------------

        else if (
                reason == InventoryReason.ORDER_CANCELLED
                        || reason == InventoryReason.ORDER_RETURNED
        ) {

            request.setChangeQty(quantity);
        }

        // -----------------------------------------------------
        // Reason không hợp lệ
        // -----------------------------------------------------

        else {

            throw new IllegalArgumentException(
                    "Lý do giao dịch không hợp lệ cho đơn hàng: "
                            + reason
            );
        }

        adjustStock(request, userId);
    }

    // =========================================================
    // GET TRANSACTIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> getTransactions(
            Long productId,
            InventoryReason reason,
            Pageable pageable
    ) {

        Page<InventoryTransaction> transactions;

        // -----------------------------------------------------
        // Filter theo product
        // -----------------------------------------------------

        if (productId != null) {

            transactions =
                    transactionRepository
                            .findByProduct_ProductId(
                                    productId,
                                    pageable
                            );
        }

        // -----------------------------------------------------
        // Filter theo reason
        // -----------------------------------------------------

        else if (reason != null) {

            transactions =
                    transactionRepository
                            .findByReason(
                                    reason,
                                    pageable
                            );
        }

        // -----------------------------------------------------
        // Không filter
        // -----------------------------------------------------

        else {

            transactions =
                    transactionRepository
                            .findAll(pageable);
        }

        return transactions.map(
                this::mapToTransactionResponse
        );
    }

    // =========================================================
    // MAP INVENTORY -> RESPONSE
    // =========================================================

    private InventoryResponse mapToInventoryResponse(
            Inventory inventory
    ) {

        Product product =
                inventory.getProduct();

        int quantityOnHand =
                inventory.getQuantityOnHand();

        int quantityReserved =
                inventory.getQuantityReserved();

        int availableQuantity =
                quantityOnHand - quantityReserved;

        boolean isLowStock =
                quantityOnHand
                        <= inventory.getLowStockThreshold();

        return InventoryResponse.builder()

                .productId(
                        product.getProductId()
                )

                .productName(
                        product.getName()
                )

                .productSku(
                        product.getSku()
                )

                .quantityOnHand(
                        quantityOnHand
                )

                .quantityReserved(
                        quantityReserved
                )

                .availableQuantity(
                        availableQuantity
                )

                .lowStockThreshold(
                        inventory.getLowStockThreshold()
                )

                .isLowStock(
                        isLowStock
                )

                .updatedAt(
                        inventory.getUpdatedAt()
                )

                .build();
    }

    // =========================================================
    // MAP TRANSACTION -> RESPONSE
    // =========================================================

    private InventoryTransactionResponse mapToTransactionResponse(
            InventoryTransaction transaction
    ) {

        Product product =
                transaction.getProduct();

        User createdBy =
                transaction.getCreatedBy();

        return InventoryTransactionResponse.builder()

                .transactionId(
                        transaction.getTransactionId()
                )

                .productId(
                        product.getProductId()
                )

                .productName(
                        product.getName()
                )

                .changeQty(
                        transaction.getChangeQty()
                )

                .reason(
                        transaction.getReason()
                )

                .referenceId(
                        transaction.getReferenceId()
                )

                .referenceType(
                        transaction.getReferenceType()
                )

                .createdById(
                        createdBy != null
                                ? createdBy.getUserId()
                                : null
                )

                .createdByName(
                        createdBy != null
                                ? createdBy.getFullName()
                                : "Hệ thống"
                )

                .createdAt(
                        transaction.getCreatedAt()
                )

                .build();
    }
}