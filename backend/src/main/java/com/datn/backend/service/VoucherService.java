package com.datn.backend.service;

import com.datn.backend.dto.request.VoucherRequest;
import com.datn.backend.dto.response.VoucherResponse;
import com.datn.backend.dto.response.VoucherValidationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface VoucherService {
    VoucherResponse create(VoucherRequest request);
    VoucherResponse update(Long id, VoucherRequest request);
    VoucherResponse disable(Long id);
    VoucherResponse getById(Long id);
    Page<VoucherResponse> getAll(Pageable pageable);
    VoucherValidationResponse validate(String code, BigDecimal orderAmount, Long userId);
}
