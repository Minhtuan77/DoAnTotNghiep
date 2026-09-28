package com.datn.backend.service;

import com.datn.backend.dto.request.CreateReviewRequest;
import com.datn.backend.dto.request.UpdateReviewRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ReviewResponse;
import com.datn.backend.entity.enums.ReviewStatus;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse create(Long userId, CreateReviewRequest request);
    ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request);
    void deleteOwn(Long userId, Long reviewId);
    PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
    PageResponse<ReviewResponse> getMyReviews(Long userId, Pageable pageable);
    PageResponse<ReviewResponse> getAllForAdmin(Pageable pageable);
    ReviewResponse updateStatus(Long reviewId, ReviewStatus status);
}
