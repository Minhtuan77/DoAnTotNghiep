package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.RecommendationPlacement;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RecommendationResponse {
    private RecommendationPlacement placement;
    private String strategy;
    private List<ProductResponse> products;
}
