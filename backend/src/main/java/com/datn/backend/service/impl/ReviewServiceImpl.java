package com.datn.backend.service.impl;

import com.datn.backend.dto.request.CreateReviewRequest;
import com.datn.backend.dto.request.UpdateReviewRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ReviewResponse;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.ReviewStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.*;
import com.datn.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewResponse create(Long userId, CreateReviewRequest request) {
        OrderItem item = orderItemRepository.findByOrderItemIdAndOrder_User_UserId(request.getOrderItemId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm đã mua trong đơn hàng của bạn"));

        if (item.getOrder().getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("Chỉ có thể đánh giá sản phẩm khi đơn hàng đã hoàn thành");
        }
        if (reviewRepository.existsByOrderItem_OrderItemId(item.getOrderItemId())) {
            throw new BusinessException("Sản phẩm trong đơn hàng này đã được đánh giá");
        }

        Review review = Review.builder()
                .product(item.getProduct())
                .user(item.getOrder().getUser())
                .orderItem(item)
                .rating(request.getRating())
                .comment(normalize(request.getComment()))
                .status(ReviewStatus.VISIBLE)
                .build();
        review = reviewRepository.save(review);
        refreshProductRating(item.getProduct().getProductId());
        return toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse update(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá của bạn"));
        review.setRating(request.getRating());
        review.setComment(normalize(request.getComment()));
        review = reviewRepository.save(review);
        refreshProductRating(review.getProduct().getProductId());
        return toResponse(review);
    }

    @Override
    @Transactional
    public void deleteOwn(Long userId, Long reviewId) {
        Review review = reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá của bạn"));
        Long productId = review.getProduct().getProductId();
        reviewRepository.delete(review);
        reviewRepository.flush();
        refreshProductRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm");
        }
        return PageResponse.from(
                reviewRepository.findByProduct_ProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.VISIBLE, pageable),
                this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        return PageResponse.from(reviewRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getAllForAdmin(Pageable pageable) {
        return PageResponse.from(reviewRepository.findAllByOrderByCreatedAtDesc(pageable), this::toResponse);
    }

    @Override
    @Transactional
    public ReviewResponse updateStatus(Long reviewId, ReviewStatus status) {
        Review review = reviewRepository.findDetailedByReviewId(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
        review.setStatus(status);
        review = reviewRepository.save(review);
        refreshProductRating(review.getProduct().getProductId());
        return toResponse(review);
    }

    private void refreshProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        long count = reviewRepository.countByProduct_ProductIdAndStatus(productId, ReviewStatus.VISIBLE);
        Double avg = reviewRepository.averageRating(productId, ReviewStatus.VISIBLE);
        product.setReviewCount(Math.toIntExact(count));
        product.setAvgRating(avg == null ? BigDecimal.ZERO : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        productRepository.save(product);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .productId(r.getProduct().getProductId())
                .productName(r.getProduct().getName())
                .orderItemId(r.getOrderItem().getOrderItemId())
                .userId(r.getUser().getUserId())
                .userName(r.getUser().getFullName())
                .userAvatarUrl(r.getUser().getAvatarUrl())
                .rating(r.getRating())
                .comment(r.getComment())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String normalize(String text) {
        if (text == null) return null;
        String value = text.trim();
        return value.isEmpty() ? null : value;
    }
}
