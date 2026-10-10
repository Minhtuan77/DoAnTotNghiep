package com.datn.backend.controller;

import com.datn.backend.dto.response.*;
import com.datn.backend.entity.enums.RecommendationPlacement;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService service;
    @GetMapping
    public ApiResponse<RecommendationResponse> get(@AuthenticationPrincipal CustomUserDetails user,
            @RequestHeader(value="X-Session-Id",required=false) String sessionId,
            @RequestParam(defaultValue="HOME") RecommendationPlacement placement,
            @RequestParam(required=false) Long anchorProductId,
            @RequestParam(defaultValue="8") int limit) {
        return ApiResponse.success("Gợi ý sản phẩm", service.recommend(user==null?null:user.getUserId(),sessionId,placement,anchorProductId,limit));
    }
    @PostMapping("/rebuild-relations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Integer> rebuild(){return ApiResponse.success("Đã xây dựng lại quan hệ sản phẩm",service.rebuildProductRelations());}
}
