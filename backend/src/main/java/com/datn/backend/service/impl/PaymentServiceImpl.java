package com.datn.backend.service.impl;

import com.datn.backend.dto.request.PaymentResultRequest;
import com.datn.backend.dto.response.PaymentTransactionResponse;
import com.datn.backend.entity.PaymentTransaction;
import com.datn.backend.entity.enums.PaymentTransactionStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.PaymentTransactionRepository;
import com.datn.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentTransactionRepository repository;

    @Override
    @Transactional
    public PaymentTransactionResponse updateResult(Long paymentId, PaymentResultRequest request) {
        PaymentTransaction p = repository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch thanh toán"));
        if (p.getStatus() != PaymentTransactionStatus.PENDING) {
            throw new BusinessException("Giao dịch thanh toán đã được xử lý");
        }
        if (request.getStatus() == PaymentTransactionStatus.PENDING) {
            throw new BusinessException("Kết quả thanh toán không thể tiếp tục là PENDING");
        }
        p.setStatus(request.getStatus());
        p.setProviderTxnCode(request.getProviderTxnCode());
        p.setRawResponse(request.getRawResponse());
        if (request.getStatus() == PaymentTransactionStatus.SUCCESS) p.setPaidAt(LocalDateTime.now());
        p = repository.save(p);
        return PaymentTransactionResponse.builder().paymentId(p.getPaymentId()).provider(p.getProvider())
                .providerTxnCode(p.getProviderTxnCode()).amount(p.getAmount()).status(p.getStatus())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt()).build();
    }
}
