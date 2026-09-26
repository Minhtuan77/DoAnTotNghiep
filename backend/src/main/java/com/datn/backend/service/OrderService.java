package com.datn.backend.service;

import com.datn.backend.dto.request.CreateOrderRequest;
import com.datn.backend.dto.request.UpdateOrderStatusRequest;
import com.datn.backend.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    Page<OrderResponse> getMyOrders(Long userId, Pageable pageable);
    OrderResponse getMyOrder(Long userId, Long orderId);
    OrderResponse cancelMyOrder(Long userId, Long orderId);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse getOrderForManagement(Long orderId);
    OrderResponse updateStatus(Long actorUserId, Long orderId, UpdateOrderStatusRequest request);
}
