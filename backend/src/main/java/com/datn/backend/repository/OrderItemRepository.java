package com.datn.backend.repository;

import com.datn.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = {"order", "order.user", "product"})
    Optional<OrderItem> findByOrderItemIdAndOrder_User_UserId(Long orderItemId, Long userId);
}
