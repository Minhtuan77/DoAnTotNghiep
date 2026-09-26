package com.datn.backend.service;

import com.datn.backend.dto.request.PaymentResultRequest;
import com.datn.backend.dto.response.PaymentTransactionResponse;

public interface PaymentService {
    PaymentTransactionResponse updateResult(Long paymentId, PaymentResultRequest request);
}
