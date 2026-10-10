package com.datn.backend.repository;

import com.datn.backend.entity.ProductRecommendation;
import com.datn.backend.entity.enums.RecommendationPlacement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {
    void deleteByUser_UserIdAndPlacement(Long userId, RecommendationPlacement placement);
}
