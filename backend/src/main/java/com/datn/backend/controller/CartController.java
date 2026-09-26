package com.datn.backend.controller;

import com.datn.backend.dto.request.AddCartItemRequest;
import com.datn.backend.dto.request.UpdateCartItemRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.CartResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", cartService.getCart(user.getUserId())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal CustomUserDetails user,
                                                              @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công", cartService.addItem(user.getUserId(), request)));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal CustomUserDetails user,
                                                                 @PathVariable Long cartItemId,
                                                                 @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật giỏ hàng thành công",
                cartService.updateItem(user.getUserId(), cartItemId, request)));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal CustomUserDetails user,
                                                                 @PathVariable Long cartItemId) {
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công",
                cartService.removeItem(user.getUserId(), cartItemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal CustomUserDetails user) {
        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng", null));
    }
}
