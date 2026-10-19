package com.datn.backend.repository;

import com.datn.backend.entity.Order;
import com.datn.backend.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Chỉ fetch một collection (items) để tránh Hibernate MultipleBagFetchException.
    // statusHistories được load lazy bên trong transaction của OrderService.
    @EntityGraph(attributePaths = {"items", "items.product", "voucher"})
    Optional<Order> findDetailedByOrderId(Long orderId);

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderIdAndUser_UserId(Long orderId, Long userId);
    boolean existsByOrderCode(String orderCode);
    List<Order> findTop10ByUser_UserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(OrderStatus status);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findTop8ByOrderByCreatedAtDesc();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumRevenueByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :start AND o.createdAt < :end")
    BigDecimal sumRevenueBetween(@Param("statuses") List<OrderStatus> statuses,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT DATE(o.created_at) AS revenue_date,
                   COALESCE(SUM(o.total_amount), 0) AS revenue,
                   COUNT(*) AS order_count
            FROM orders o
            WHERE o.status IN ('DELIVERED', 'COMPLETED')
              AND o.created_at >= :start
              AND o.created_at < :end
            GROUP BY DATE(o.created_at)
            ORDER BY revenue_date ASC
            """, nativeQuery = true)
    List<Object[]> revenueByDay(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
