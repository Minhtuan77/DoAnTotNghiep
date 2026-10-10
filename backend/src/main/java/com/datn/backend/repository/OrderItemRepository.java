package com.datn.backend.repository;

import com.datn.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = {"order", "order.user", "product"})
    Optional<OrderItem> findByOrderItemIdAndOrder_User_UserId(Long orderItemId, Long userId);

    @Query(value = """
        SELECT oi2.product_id, COUNT(DISTINCT oi1.order_id) AS pair_count
        FROM order_items oi1
        JOIN order_items oi2 ON oi1.order_id = oi2.order_id AND oi1.product_id <> oi2.product_id
        JOIN orders o ON o.order_id = oi1.order_id
        WHERE oi1.product_id = :productId AND o.status IN ('DELIVERED','COMPLETED')
        GROUP BY oi2.product_id
        ORDER BY pair_count DESC
        LIMIT 8
        """, nativeQuery = true)
    List<Object[]> findCoPurchasedProductIds(@Param("productId") Long productId);
}
