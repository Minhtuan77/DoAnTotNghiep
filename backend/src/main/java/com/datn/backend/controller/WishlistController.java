package com.datn.backend.controller;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.WishlistResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WishlistResponse>>> getMine(@AuthenticationPrincipal CustomUserDetails user,
                                                                                Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu thích thành công", wishlistService.getMine(user.getUserId(), pageable)));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(@AuthenticationPrincipal CustomUserDetails user,
                                                              @PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm sản phẩm vào yêu thích", wishlistService.add(user.getUserId(), productId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(@AuthenticationPrincipal CustomUserDetails user,
                                                     @PathVariable Long productId) {
        wishlistService.remove(user.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm khỏi yêu thích", null));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> check(@AuthenticationPrincipal CustomUserDetails user,
                                                                    @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra yêu thích thành công",
                Map.of("wishlisted", wishlistService.contains(user.getUserId(), productId))));
    }
}
