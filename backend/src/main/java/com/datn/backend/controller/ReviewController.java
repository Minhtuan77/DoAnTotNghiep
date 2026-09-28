package com.datn.backend.controller;

import com.datn.backend.dto.request.CreateReviewRequest;
import com.datn.backend.dto.request.UpdateReviewRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ReviewResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> productReviews(@PathVariable Long productId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá sản phẩm thành công", reviewService.getProductReviews(productId, pageable)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> myReviews(@AuthenticationPrincipal CustomUserDetails user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá của tôi thành công", reviewService.getMyReviews(user.getUserId(), pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@AuthenticationPrincipal CustomUserDetails user,
                                                               @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đánh giá sản phẩm thành công", reviewService.create(user.getUserId(), request)));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(@AuthenticationPrincipal CustomUserDetails user,
                                                               @PathVariable Long reviewId,
                                                               @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đánh giá thành công", reviewService.update(user.getUserId(), reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long reviewId) {
        reviewService.deleteOwn(user.getUserId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success("Xóa đánh giá thành công", null));
    }
}
