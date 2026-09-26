package com.datn.backend.controller;

import com.datn.backend.dto.response.VNPayPaymentResponse;
import com.datn.backend.dto.response.VNPayReturnResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;

    @PostMapping("/create/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VNPayPaymentResponse> createPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        return ResponseEntity.ok(
                vnPayService.createPaymentUrl(orderId, userDetails.getUserId(), request));
    }

    /** VNPAY server-to-server callback. Đây là nơi DUY NHẤT cập nhật kết quả thanh toán. */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.processIpn(params));
    }

    /** Browser được VNPAY redirect về đây. Chỉ verify + hiển thị kết quả, không cập nhật DB. */
    @GetMapping("/return")
    public ResponseEntity<VNPayReturnResponse> returnUrl(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.processReturn(params));
    }
}
