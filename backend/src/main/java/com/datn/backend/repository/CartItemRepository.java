package com.datn.backend.repository;

import com.datn.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);
    Optional<CartItem> findByCartItemIdAndCart_User_UserId(Long cartItemId, Long userId);
    void deleteAllByCart_User_UserId(Long userId);
}
