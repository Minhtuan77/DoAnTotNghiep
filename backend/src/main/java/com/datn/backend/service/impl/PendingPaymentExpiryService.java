package com.datn.backend.service.impl;

import com.datn.backend.config.VNPayConfig;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.InventoryReason;
import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.PaymentTransactionStatus;
import com.datn.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Tự động giải phóng kho/voucher của đơn VNPAY bị bỏ dở.
 * Dùng latest VNPAY payment để một lần retry tạo payment mới sẽ có đủ thời gian thanh toán.
 */
@Service
@RequiredArgsConstructor
public class PendingPaymentExpiryService {
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VNPayConfig config;
    private final PaymentTransactionRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    @Scheduled(fixedDelayString = "${order.pending-payment-expiry-check-ms:60000}")
    @Transactional
    public void expirePendingVnpayOrders() {
        LocalDateTime cutoff = LocalDateTime.now(VN_ZONE).minusMinutes(config.getExpireMinutes());
        List<Long> ids = paymentRepository.findExpiredLatestVnpayPaymentIds(cutoff, OrderStatus.PENDING_PAYMENT);
        for (Long paymentId : ids) {
            expireOne(paymentId, cutoff);
        }
    }

    @Transactional
    public void expireOne(Long paymentId, LocalDateTime cutoff) {
        PaymentTransaction payment = paymentRepository.findByIdForUpdate(paymentId).orElse(null);
        if (payment == null || !"VNPAY".equalsIgnoreCase(payment.getProvider())) return;
        if (payment.getCreatedAt() == null || !payment.getCreatedAt().isBefore(cutoff)) return;

        Order order = payment.getOrder();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return;

        // Nếu trong lúc scheduler chờ lock đã có payment mới hơn, không hủy đơn.
        PaymentTransaction latest = paymentRepository
                .findByOrder_OrderIdOrderByCreatedAtAsc(order.getOrderId())
                .stream().filter(p -> "VNPAY".equalsIgnoreCase(p.getProvider()))
                .reduce((a, b) -> b).orElse(payment);
        if (!latest.getPaymentId().equals(payment.getPaymentId())) return;

        if (payment.getStatus() == PaymentTransactionStatus.PENDING) {
            payment.setStatus(PaymentTransactionStatus.CANCELLED);
            paymentRepository.save(payment);
        }

        OrderStatus old = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        historyRepository.save(OrderStatusHistory.builder()
                .order(order).fromStatus(old.name()).toStatus(OrderStatus.CANCELLED.name())
                .changedBy(null).note("Hệ thống tự hủy do hết thời gian thanh toán VNPAY").build());

        for (OrderItem item : order.getItems()) {
            Inventory inv = inventoryRepository.findByProductIdForUpdate(item.getProduct().getProductId()).orElse(null);
            if (inv != null) {
                inv.setQuantityOnHand(inv.getQuantityOnHand() + item.getQuantity());
                inventoryRepository.save(inv);
                inventoryTransactionRepository.save(InventoryTransaction.builder()
                        .product(item.getProduct()).changeQty(item.getQuantity())
                        .reason(InventoryReason.ORDER_CANCELLED).referenceType("ORDER")
                        .referenceId(order.getOrderId()).createdBy(null).build());
            }
        }

        voucherUsageRepository.findByOrder_OrderId(order.getOrderId()).ifPresent(usage -> {
            Voucher voucher = usage.getVoucher();
            voucherUsageRepository.delete(usage);
            voucher.setUsedCount(Math.max(0, voucher.getUsedCount() - 1));
            voucherRepository.save(voucher);
        });
    }
}
