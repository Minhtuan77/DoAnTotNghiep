package com.datn.backend.service;

import com.datn.backend.dto.response.RecommendationResponse;
import com.datn.backend.entity.enums.RecommendationPlacement;

public interface RecommendationService {
    RecommendationResponse recommend(Long userId, String sessionId, RecommendationPlacement placement, Long anchorProductId, int limit);
    int rebuildProductRelations();
}
