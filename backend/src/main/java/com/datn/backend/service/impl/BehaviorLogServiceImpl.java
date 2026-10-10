package com.datn.backend.service.impl;

import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.BehaviorActionType;
import com.datn.backend.repository.*;
import com.datn.backend.service.BehaviorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BehaviorLogServiceImpl implements BehaviorLogService {
    private final UserBehaviorLogRepository behaviorRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void record(Long userId, String sessionId, Long productId, BehaviorActionType actionType, String searchKeyword, String metadata) {
        if (actionType == null) return;
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        Product product = productId == null ? null : productRepository.findById(productId).orElse(null);
        if (requiresProduct(actionType) && product == null) return;
        String sid = normalize(sessionId, 100);
        String keyword = normalize(searchKeyword, 255);
        behaviorRepository.save(UserBehaviorLog.builder()
                .user(user).sessionId(sid).product(product).actionType(actionType)
                .searchKeyword(keyword).metadata(metadata).build());
        if (product != null && actionType == BehaviorActionType.VIEW) {
            product.setViewCount((product.getViewCount() == null ? 0 : product.getViewCount()) + 1);
            productRepository.save(product);
        }
    }

    private boolean requiresProduct(BehaviorActionType type) {
        return type != BehaviorActionType.SEARCH;
    }
    private String normalize(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim(); return v.length() > max ? v.substring(0, max) : v;
    }
}
