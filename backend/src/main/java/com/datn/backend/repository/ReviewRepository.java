package com.datn.backend.repository;

import com.datn.backend.entity.Review;
import com.datn.backend.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    @EntityGraph(attributePaths = {"product", "user", "orderItem"})
    Page<Review> findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(Long productId, ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "orderItem"})
    Page<Review> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "orderItem"})
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"product", "user", "orderItem"})
    Optional<Review> findByReviewIdAndUser_UserId(Long reviewId, Long userId);

    @EntityGraph(attributePaths = {"product", "user", "orderItem"})
    Optional<Review> findDetailedByReviewId(Long reviewId);

    long countByProduct_ProductIdAndStatus(Long productId, ReviewStatus status);

    @Query("select avg(r.rating) from Review r where r.product.productId = :productId and r.status = :status")
    Double averageRating(@Param("productId") Long productId, @Param("status") ReviewStatus status);
}
