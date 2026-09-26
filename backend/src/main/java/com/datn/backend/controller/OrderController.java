package com.datn.backend.controller;

import com.datn.backend.dto.request.CreateOrderRequest;
import com.datn.backend.dto.response.OrderResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal CustomUserDetails user,
                                                @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> myOrders(@AuthenticationPrincipal CustomUserDetails user,
                                                        Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(user.getUserId(), pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> detail(@AuthenticationPrincipal CustomUserDetails user,
                                                @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getMyOrder(user.getUserId(), orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal CustomUserDetails user,
                                                @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelMyOrder(user.getUserId(), orderId));
    }
}
