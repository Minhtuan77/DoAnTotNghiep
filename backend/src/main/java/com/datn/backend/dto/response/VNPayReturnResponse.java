package com.datn.backend.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class VNPayReturnResponse {
    private boolean signatureValid;
    private boolean paymentSuccessful;
    private String responseCode;
    private String transactionStatus;
    private String transactionNo;
    private String txnRef;
    private String message;
}
