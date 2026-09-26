package com.datn.backend.service.impl;

import com.datn.backend.config.VNPayConfig;
import com.datn.backend.dto.response.VNPayPaymentResponse;
import com.datn.backend.dto.response.VNPayReturnResponse;
import com.datn.backend.entity.Order;
import com.datn.backend.entity.OrderStatusHistory;
import com.datn.backend.entity.PaymentTransaction;
import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.PaymentMethod;
import com.datn.backend.entity.enums.PaymentTransactionStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.repository.OrderRepository;
import com.datn.backend.repository.OrderStatusHistoryRepository;
import com.datn.backend.repository.PaymentTransactionRepository;
import com.datn.backend.service.VNPayService;
import com.datn.backend.util.VNPayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VNPayConfig config;
    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public VNPayPaymentResponse createPaymentUrl(Long orderId, Long userId, HttpServletRequest request) {
        validateConfig();

        Order order = orderRepository.findByOrderIdAndUser_UserId(orderId, userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng của bạn"));

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BusinessException("Đơn hàng này không sử dụng thanh toán ONLINE");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Đơn hàng không ở trạng thái chờ thanh toán");
        }

        PaymentTransaction payment = paymentRepository
                .findFirstByOrder_OrderIdAndProviderAndStatusOrderByCreatedAtDesc(
                        orderId, "VNPAY", PaymentTransactionStatus.PENDING)
                .orElseGet(() -> paymentRepository.save(PaymentTransaction.builder()
                        .order(order)
                        .provider("VNPAY")
                        .amount(order.getTotalAmount())
                        .status(PaymentTransactionStatus.PENDING)
                        .build()));

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", config.getVersion());
        params.put("vnp_Command", config.getCommand());
        params.put("vnp_TmnCode", config.getTmnCode());
        params.put("vnp_Amount", toVnpAmount(payment.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getPaymentId().toString());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_OrderType", config.getOrderType());
        params.put("vnp_Locale", config.getLocale());
        params.put("vnp_ReturnUrl", config.getReturnUrl());
        params.put("vnp_IpAddr", getClientIp(request));
        params.put("vnp_CreateDate", now.format(VNP_DATE));
        params.put("vnp_ExpireDate", now.plusMinutes(config.getExpireMinutes()).format(VNP_DATE));

        String query = VNPayUtil.buildQueryString(params);
        String secureHash = VNPayUtil.hmacSHA512(config.getHashSecret(), query);
        String paymentUrl = config.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        return VNPayPaymentResponse.builder()
                .orderId(orderId)
                .paymentId(payment.getPaymentId())
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        try {
            validateConfig();
            if (!VNPayUtil.verifySignature(params, config.getHashSecret())) {
                return ipn("97", "Invalid signature");
            }
            if (!config.getTmnCode().equals(params.get("vnp_TmnCode"))) {
                return ipn("97", "Invalid terminal code");
            }

            Long paymentId;
            try {
                paymentId = Long.valueOf(params.get("vnp_TxnRef"));
            } catch (Exception e) {
                return ipn("01", "Order not found");
            }

            PaymentTransaction payment = paymentRepository.findByIdForUpdate(paymentId).orElse(null);
            if (payment == null || !"VNPAY".equalsIgnoreCase(payment.getProvider())) {
                return ipn("01", "Order not found");
            }

            if (!toVnpAmount(payment.getAmount()).equals(params.get("vnp_Amount"))) {
                return ipn("04", "Invalid amount");
            }

            if (payment.getStatus() != PaymentTransactionStatus.PENDING) {
                return ipn("02", "Order already confirmed");
            }

            boolean success = "00".equals(params.get("vnp_ResponseCode"))
                    && "00".equals(params.get("vnp_TransactionStatus"));

            payment.setProviderTxnCode(params.get("vnp_TransactionNo"));
            payment.setRawResponse(toJson(params));
            payment.setStatus(success ? PaymentTransactionStatus.SUCCESS : PaymentTransactionStatus.FAILED);
            if (success) payment.setPaidAt(LocalDateTime.now(VN_ZONE));
            paymentRepository.save(payment);

            if (success) {
                Order order = payment.getOrder();
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                    OrderStatus old = order.getStatus();
                    order.setStatus(OrderStatus.PENDING_CONFIRMATION);
                    orderRepository.save(order);
                    historyRepository.save(OrderStatusHistory.builder()
                            .order(order)
                            .fromStatus(old.name())
                            .toStatus(OrderStatus.PENDING_CONFIRMATION.name())
                            .changedBy(null)
                            .note("VNPAY xác nhận thanh toán thành công")
                            .build());
                }
            }

            return ipn("00", "Confirm Success");
        } catch (Exception e) {
            return ipn("99", "Unknown error");
        }
    }

    @Override
    public VNPayReturnResponse processReturn(Map<String, String> params) {
        validateConfig();
        boolean signatureValid = VNPayUtil.verifySignature(params, config.getHashSecret());
        boolean successful = signatureValid
                && "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        return VNPayReturnResponse.builder()
                .signatureValid(signatureValid)
                .paymentSuccessful(successful)
                .responseCode(params.get("vnp_ResponseCode"))
                .transactionStatus(params.get("vnp_TransactionStatus"))
                .transactionNo(params.get("vnp_TransactionNo"))
                .txnRef(params.get("vnp_TxnRef"))
                .message(!signatureValid ? "Chữ ký VNPAY không hợp lệ"
                        : successful ? "Thanh toán thành công"
                        : "Thanh toán không thành công")
                .build();
    }

    private String toVnpAmount(BigDecimal amount) {
        try {
            return amount.multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString();
        } catch (ArithmeticException e) {
            throw new BusinessException("Số tiền thanh toán không hợp lệ cho VNPAY");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return (ip == null || ip.isBlank()) ? "127.0.0.1" : ip;
    }

    private void validateConfig() {
        if (config.getTmnCode() == null || config.getTmnCode().isBlank()
                || config.getHashSecret() == null || config.getHashSecret().isBlank()) {
            throw new BusinessException("Chưa cấu hình VNPAY_TMN_CODE hoặc VNPAY_HASH_SECRET");
        }
    }

    private Map<String, String> ipn(String code, String message) {
        Map<String, String> result = new HashMap<>();
        result.put("RspCode", code);
        result.put("Message", message);
        return result;
    }

    private String toJson(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
