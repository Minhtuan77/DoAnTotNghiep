package com.datn.backend.controller;

import com.datn.backend.dto.request.BehaviorLogRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.BehaviorLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorController {
    private final BehaviorLogService behaviorLogService;
    @PostMapping
    public ApiResponse<Void> log(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody BehaviorLogRequest request) {
        behaviorLogService.record(user==null?null:user.getUserId(), request.getSessionId(), request.getProductId(), request.getActionType(), request.getSearchKeyword(), request.getMetadata());
        return ApiResponse.success("Đã ghi nhận hành vi", null);
    }
}
