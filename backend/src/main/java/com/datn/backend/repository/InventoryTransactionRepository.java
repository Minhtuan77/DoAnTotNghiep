package com.datn.backend.repository;

import com.datn.backend.entity.InventoryTransaction;
import com.datn.backend.entity.enums.InventoryReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    Page<InventoryTransaction> findByProduct_ProductId(Long productId, Pageable pageable);
    
    Page<InventoryTransaction> findByReason(InventoryReason reason, Pageable pageable);
}