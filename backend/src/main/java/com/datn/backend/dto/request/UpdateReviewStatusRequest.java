package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewStatusRequest {
    @NotNull(message = "Trạng thái đánh giá không được để trống")
    private ReviewStatus status;
}
