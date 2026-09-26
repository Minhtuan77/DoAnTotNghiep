package com.datn.backend.repository;

import com.datn.backend.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    long countByVoucher_VoucherIdAndUser_UserId(Long voucherId, Long userId);
    boolean existsByVoucher_VoucherIdAndOrder_OrderId(Long voucherId, Long orderId);
    Optional<VoucherUsage> findByOrder_OrderId(Long orderId);
}
