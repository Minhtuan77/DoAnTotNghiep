package com.datn.backend.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VNPayPaymentResponse {
    private Long orderId;
    private Long paymentId;
    private String paymentUrl;
}
