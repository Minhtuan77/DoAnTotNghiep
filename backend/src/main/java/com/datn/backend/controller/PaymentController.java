package com.datn.backend.controller;

import com.datn.backend.dto.response.VNPayPaymentResponse;
import com.datn.backend.dto.response.VNPayReturnResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayService vnPayService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping("/create/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VNPayPaymentResponse> createPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        return ResponseEntity.ok(
                vnPayService.createPaymentUrl(orderId, userDetails.getUserId(), request));
    }

    /** VNPAY server-to-server callback. Đây là nơi cập nhật kết quả thanh toán. */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(vnPayService.processIpn(params));
    }

    /**
     * Browser được VNPAY redirect về backend để kiểm tra chữ ký, sau đó backend
     * chuyển tiếp sang màn hình kết quả ở frontend. Không đưa hash secret ra trình duyệt.
     */
    @GetMapping("/return")
    public ResponseEntity<Void> returnUrl(@RequestParam Map<String, String> params) {
        VNPayReturnResponse result = vnPayService.processReturn(params);

        URI redirectUri = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/payment-result")
                .queryParam("success", result.isPaymentSuccessful())
                .queryParam("signatureValid", result.isSignatureValid())
                .queryParam("responseCode", safe(result.getResponseCode()))
                .queryParam("transactionStatus", safe(result.getTransactionStatus()))
                .queryParam("transactionNo", safe(result.getTransactionNo()))
                .queryParam("txnRef", safe(result.getTxnRef()))
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUri.toString())
                .build();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
