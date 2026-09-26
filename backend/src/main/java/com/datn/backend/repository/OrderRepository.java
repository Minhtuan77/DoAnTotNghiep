package com.datn.backend.repository;

import com.datn.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"items", "items.product", "statusHistories", "statusHistories.changedBy", "voucher"})
    Optional<Order> findDetailedByOrderId(Long orderId);

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderIdAndUser_UserId(Long orderId, Long userId);
    boolean existsByOrderCode(String orderCode);
}
