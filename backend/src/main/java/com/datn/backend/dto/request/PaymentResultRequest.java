package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.PaymentTransactionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentResultRequest {
    @NotNull
    private PaymentTransactionStatus status;
    @Size(max = 100)
    private String providerTxnCode;
    private String rawResponse;
}
