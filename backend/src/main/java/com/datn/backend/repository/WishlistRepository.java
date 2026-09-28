package com.datn.backend.repository;

import com.datn.backend.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    boolean existsByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    @EntityGraph(attributePaths = {"product"})
    Page<Wishlist> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Wishlist> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    long deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
}
