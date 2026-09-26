package com.datn.backend.service;

import com.datn.backend.dto.request.AddCartItemRequest;
import com.datn.backend.dto.request.UpdateCartItemRequest;
import com.datn.backend.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addItem(Long userId, AddCartItemRequest request);
    CartResponse updateItem(Long userId, Long cartItemId, UpdateCartItemRequest request);
    CartResponse removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
