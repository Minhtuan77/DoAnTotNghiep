package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.PaymentTransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class PaymentTransactionResponse {
    private Long paymentId;
    private String provider;
    private String providerTxnCode;
    private BigDecimal amount;
    private PaymentTransactionStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
