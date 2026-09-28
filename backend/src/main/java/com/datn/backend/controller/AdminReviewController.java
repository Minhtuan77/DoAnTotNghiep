package com.datn.backend.controller;

import com.datn.backend.dto.request.UpdateReviewStatusRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ReviewResponse;
import com.datn.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasAuthority('REVIEW_READ')")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đánh giá thành công", reviewService.getAllForAdmin(pageable)));
    }

    @PatchMapping("/{reviewId}/status")
    @PreAuthorize("hasAuthority('REVIEW_UPDATE')")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateStatus(@PathVariable Long reviewId,
                                                                     @Valid @RequestBody UpdateReviewStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đánh giá thành công",
                reviewService.updateStatus(reviewId, request.getStatus())));
    }
}
