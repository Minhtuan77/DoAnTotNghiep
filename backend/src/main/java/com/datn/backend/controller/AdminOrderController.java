package com.datn.backend.controller;

import com.datn.backend.dto.request.UpdateOrderStatusRequest;
import com.datn.backend.dto.response.OrderResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<Page<OrderResponse>> all(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ORDER_READ')")
    public ResponseEntity<OrderResponse> detail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderForManagement(orderId));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<OrderResponse> updateStatus(@AuthenticationPrincipal CustomUserDetails user,
                                                      @PathVariable Long orderId,
                                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(user.getUserId(), orderId, request));
    }
}
