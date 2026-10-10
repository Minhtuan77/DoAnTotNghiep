package com.datn.backend.service;

import com.datn.backend.entity.enums.BehaviorActionType;

public interface BehaviorLogService {
    void record(Long userId, String sessionId, Long productId, BehaviorActionType actionType, String searchKeyword, String metadata);
}
