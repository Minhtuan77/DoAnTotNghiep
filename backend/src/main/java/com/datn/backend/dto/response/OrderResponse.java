package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class OrderResponse {
    private Long orderId;
    private String orderCode;
    private Long userId;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;
    private PaymentMethod paymentMethod;
    private OrderStatus status;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private Long voucherId;
    private String voucherCode;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistories;
    private List<PaymentTransactionResponse> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
