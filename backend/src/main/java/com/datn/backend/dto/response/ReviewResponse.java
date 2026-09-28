package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.ReviewStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReviewResponse {
    private Long reviewId;
    private Long productId;
    private String productName;
    private Long orderItemId;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private Short rating;
    private String comment;
    private ReviewStatus status;
    private LocalDateTime createdAt;
}
