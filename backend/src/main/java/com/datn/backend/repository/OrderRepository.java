package com.datn.backend.repository;

import com.datn.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Chỉ fetch một collection (items) để tránh Hibernate MultipleBagFetchException.
    // statusHistories được load lazy bên trong transaction của OrderService.
    @EntityGraph(attributePaths = {"items", "items.product", "voucher"})
    Optional<Order> findDetailedByOrderId(Long orderId);

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderIdAndUser_UserId(Long orderId, Long userId);
    boolean existsByOrderCode(String orderCode);
    List<Order> findTop10ByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
