package com.datn.backend.service;

import com.datn.backend.dto.response.VNPayPaymentResponse;
import com.datn.backend.dto.response.VNPayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {
    VNPayPaymentResponse createPaymentUrl(Long orderId, Long userId, HttpServletRequest request);
    Map<String, String> processIpn(Map<String, String> params);
    VNPayReturnResponse processReturn(Map<String, String> params);
}
