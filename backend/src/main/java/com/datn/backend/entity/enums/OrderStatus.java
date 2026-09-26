package com.datn.backend.entity.enums;

public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_CONFIRMATION,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    RETURNED
}
