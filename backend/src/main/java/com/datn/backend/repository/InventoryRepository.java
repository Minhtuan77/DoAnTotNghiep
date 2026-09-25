package com.datn.backend.repository;

import com.datn.backend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Tìm thông tin kho theo ID sản phẩm.
     */
    Optional<Inventory> findByProduct_ProductId(Long productId);

    /**
     * Truy vấn danh sách sản phẩm cảnh báo sắp hết hàng (tồn kho dưới hoặc bằng ngưỡng low_stock_threshold).
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityOnHand <= i.lowStockThreshold")
    Page<Inventory> findLowStockProducts(Pageable pageable);
}