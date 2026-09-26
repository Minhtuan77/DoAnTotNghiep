package com.datn.backend.service.impl;

import com.datn.backend.dto.request.CreateOrderRequest;
import com.datn.backend.dto.request.UpdateOrderStatusRequest;
import com.datn.backend.dto.response.*;
import com.datn.backend.entity.*;
import com.datn.backend.entity.enums.*;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.*;
import com.datn.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final CartRepository cartRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Cart cart = cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException("Giỏ hàng đang trống"));
        if (cart.getItems().isEmpty()) throw new BusinessException("Giỏ hàng đang trống");

        if (request.getPaymentMethod() == PaymentMethod.ONLINE) {
            if (request.getPaymentProvider() == null || request.getPaymentProvider().isBlank()) {
                throw new BusinessException("Vui lòng chọn cổng thanh toán khi thanh toán ONLINE");
            }
            if (!"VNPAY".equalsIgnoreCase(request.getPaymentProvider().trim())) {
                throw new BusinessException("Hiện tại hệ thống chỉ hỗ trợ VNPAY cho thanh toán ONLINE");
            }
        }

        // Lock từng inventory trước khi kiểm tra/trừ để tránh oversell khi nhiều đơn đặt đồng thời.
        Map<Long, Inventory> inventories = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException("Sản phẩm " + product.getName() + " hiện không thể mua");
            }
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(product.getProductId())
                    .orElseThrow(() -> new BusinessException("Sản phẩm " + product.getName() + " chưa có thông tin tồn kho"));
            int available = inventory.getQuantityOnHand() - inventory.getQuantityReserved();
            if (cartItem.getQuantity() > available) {
                throw new BusinessException("Sản phẩm " + product.getName() + " chỉ còn " + available + " sản phẩm khả dụng");
            }
            inventories.put(product.getProductId(), inventory);
            BigDecimal unitPrice = effectivePrice(product);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        BigDecimal shippingFee = BigDecimal.ZERO; // SRS chưa quy định công thức phí vận chuyển.
        Voucher voucher = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            voucher = voucherRepository.findByCodeForUpdate(request.getVoucherCode().trim())
                    .orElseThrow(() -> new BusinessException("Mã voucher không tồn tại"));
            discount = validateAndCalculateVoucher(voucher, subtotal);
        }
        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .recipientName(request.getRecipientName().trim())
                .recipientPhone(request.getRecipientPhone().trim())
                .shippingAddress(request.getShippingAddress().trim())
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getPaymentMethod() == PaymentMethod.ONLINE
                        ? OrderStatus.PENDING_PAYMENT
                        : OrderStatus.PENDING_CONFIRMATION)
                .subtotalAmount(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discount)
                .totalAmount(total)
                .voucher(voucher)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal unitPrice = effectivePrice(product);
            OrderItem item = OrderItem.builder()
                    .order(order).product(product)
                    .productNameSnapshot(product.getName())
                    .unitPriceSnapshot(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineSubtotal(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            order.getItems().add(item);
        }
        order = orderRepository.save(order);

        OrderStatusHistory createdHistory = OrderStatusHistory.builder()
                .order(order).fromStatus(null).toStatus(order.getStatus().name())
                .changedBy(user).note(request.getPaymentMethod() == PaymentMethod.ONLINE
                        ? "Khách hàng tạo đơn hàng - chờ thanh toán VNPAY"
                        : "Khách hàng tạo đơn hàng").build();
        order.getStatusHistories().add(createdHistory);
        historyRepository.save(createdHistory);

        // Trừ kho + ghi audit trong cùng transaction.
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventories.get(item.getProduct().getProductId());
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() - item.getQuantity());
            inventoryRepository.save(inventory);
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .product(item.getProduct()).changeQty(-item.getQuantity())
                    .reason(InventoryReason.ORDER_PLACED).referenceType("ORDER")
                    .referenceId(order.getOrderId()).createdBy(user).build());
        }

        if (voucher != null) {
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
            voucherUsageRepository.save(VoucherUsage.builder()
                    .voucher(voucher).user(user).order(order).build());
        }

        PaymentTransaction payment = PaymentTransaction.builder()
                .order(order)
                .provider(request.getPaymentMethod() == PaymentMethod.COD ? "COD" : request.getPaymentProvider().trim().toUpperCase(Locale.ROOT))
                .amount(total)
                .status(PaymentTransactionStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // Chỉ xóa item; giữ Cart để user tiếp tục mua hàng sau này.
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndUser_UserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndUser_UserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT
                && order.getStatus() != OrderStatus.PENDING_CONFIRMATION) {
            throw new BusinessException("Chỉ có thể hủy đơn hàng đang chờ thanh toán hoặc chờ xác nhận");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        changeStatus(order, OrderStatus.CANCELLED, user, "Khách hàng hủy đơn");
        restoreStock(order, InventoryReason.ORDER_CANCELLED, user);
        rollbackVoucherUsage(order);
        cancelPendingPayments(order);
        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse getOrderForManagement(Long orderId) {
        return toResponse(getOrder(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long actorUserId, Long orderId, UpdateOrderStatusRequest request) {
        Order order = getOrder(orderId);
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người thực hiện"));
        OrderStatus target = request.getStatus();
        if (!isAllowedTransition(order.getStatus(), target)) {
            throw new BusinessException("Không thể chuyển trạng thái từ " + order.getStatus() + " sang " + target);
        }
        OrderStatus old = order.getStatus();
        changeStatus(order, target, actor, request.getNote());
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order, InventoryReason.ORDER_CANCELLED, actor);
            rollbackVoucherUsage(order);
            cancelPendingPayments(order);
        } else if (target == OrderStatus.RETURNED) {
            restoreStock(order, InventoryReason.ORDER_RETURNED, actor);
        } else if (target == OrderStatus.COMPLETED && order.getPaymentMethod() == PaymentMethod.COD) {
            markCodPaid(order);
        }
        return toResponse(order);
    }

    private void changeStatus(Order order, OrderStatus target, User actor, String note) {
        OrderStatus old = order.getStatus();
        order.setStatus(target);
        orderRepository.save(order);
        historyRepository.save(OrderStatusHistory.builder().order(order)
                .fromStatus(old == null ? null : old.name()).toStatus(target.name())
                .changedBy(actor).note(note).build());
    }

    private boolean isAllowedTransition(OrderStatus from, OrderStatus to) {
        if (from == to) return false;
        return switch (from) {
            case PENDING_PAYMENT -> to == OrderStatus.CANCELLED;
            case PENDING_CONFIRMATION -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
            case SHIPPING -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.COMPLETED || to == OrderStatus.RETURNED;
            case COMPLETED -> to == OrderStatus.RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }

    private void restoreStock(Order order, InventoryReason reason, User actor) {
        for (OrderItem item : order.getItems()) {
            Inventory inv = inventoryRepository.findByProductIdForUpdate(item.getProduct().getProductId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tồn kho của sản phẩm " + item.getProductNameSnapshot()));
            inv.setQuantityOnHand(inv.getQuantityOnHand() + item.getQuantity());
            inventoryRepository.save(inv);
            inventoryTransactionRepository.save(InventoryTransaction.builder()
                    .product(item.getProduct()).changeQty(item.getQuantity()).reason(reason)
                    .referenceType("ORDER").referenceId(order.getOrderId()).createdBy(actor).build());
        }
    }

    private void rollbackVoucherUsage(Order order) {
        if (order.getVoucher() == null) return;
        voucherUsageRepository.findByOrder_OrderId(order.getOrderId()).ifPresent(u -> {
                    voucherUsageRepository.delete(u);
                    Voucher v = order.getVoucher();
                    v.setUsedCount(Math.max(0, v.getUsedCount() - 1));
                    voucherRepository.save(v);
                });
    }

    private void cancelPendingPayments(Order order) {
        for (PaymentTransaction p : paymentRepository.findByOrder_OrderIdOrderByCreatedAtAsc(order.getOrderId())) {
            if (p.getStatus() == PaymentTransactionStatus.PENDING) {
                p.setStatus(PaymentTransactionStatus.CANCELLED);
                paymentRepository.save(p);
            }
        }
    }

    private void markCodPaid(Order order) {
        paymentRepository.findByOrder_OrderIdOrderByCreatedAtAsc(order.getOrderId()).stream()
                .filter(p -> "COD".equals(p.getProvider()) && p.getStatus() == PaymentTransactionStatus.PENDING)
                .findFirst().ifPresent(p -> {
                    p.setStatus(PaymentTransactionStatus.SUCCESS);
                    p.setPaidAt(LocalDateTime.now());
                    paymentRepository.save(p);
                });
    }

    private BigDecimal validateAndCalculateVoucher(Voucher v, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (v.getStatus() != VoucherStatus.ACTIVE) throw new BusinessException("Voucher không còn hoạt động");
        if (now.isBefore(v.getStartDate())) throw new BusinessException("Voucher chưa đến thời gian sử dụng");
        if (now.isAfter(v.getEndDate())) throw new BusinessException("Voucher đã hết hạn");
        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) throw new BusinessException("Voucher đã đạt giới hạn sử dụng");
        if (subtotal.compareTo(v.getMinOrderValue()) < 0) throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu " + v.getMinOrderValue());
        BigDecimal discount;
        if (v.getDiscountType() == DiscountType.PERCENT) {
            discount = subtotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (v.getMaxDiscountValue() != null && discount.compareTo(v.getMaxDiscountValue()) > 0) discount = v.getMaxDiscountValue();
        } else discount = v.getDiscountValue();
        return discount.min(subtotal);
    }

    private BigDecimal effectivePrice(Product p) {
        return p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
    }

    private String generateOrderCode() {
        String code;
        do {
            code = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                    + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
        } while (orderRepository.existsByOrderCode(code));
        return code;
    }

    private Order getOrder(Long id) {
        return orderRepository.findDetailedByOrderId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(i -> OrderItemResponse.builder()
                .orderItemId(i.getOrderItemId()).productId(i.getProduct().getProductId())
                .productName(i.getProductNameSnapshot()).unitPrice(i.getUnitPriceSnapshot())
                .quantity(i.getQuantity()).lineSubtotal(i.getLineSubtotal()).build()).toList();
        List<OrderStatusHistoryResponse> histories = o.getStatusHistories().stream()
                .sorted(Comparator.comparing(OrderStatusHistory::getChangedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(h -> OrderStatusHistoryResponse.builder().historyId(h.getHistoryId()).fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus()).changedById(h.getChangedBy() == null ? null : h.getChangedBy().getUserId())
                        .changedByName(h.getChangedBy() == null ? "Hệ thống" : h.getChangedBy().getFullName())
                        .note(h.getNote()).changedAt(h.getChangedAt()).build()).toList();
        List<PaymentTransactionResponse> payments = paymentRepository.findByOrder_OrderIdOrderByCreatedAtAsc(o.getOrderId()).stream()
                .map(p -> PaymentTransactionResponse.builder().paymentId(p.getPaymentId()).provider(p.getProvider())
                        .providerTxnCode(p.getProviderTxnCode()).amount(p.getAmount()).status(p.getStatus())
                        .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt()).build()).toList();
        return OrderResponse.builder().orderId(o.getOrderId()).orderCode(o.getOrderCode()).userId(o.getUser().getUserId())
                .recipientName(o.getRecipientName()).recipientPhone(o.getRecipientPhone()).shippingAddress(o.getShippingAddress())
                .note(o.getNote()).paymentMethod(o.getPaymentMethod()).status(o.getStatus()).subtotalAmount(o.getSubtotalAmount())
                .shippingFee(o.getShippingFee()).discountAmount(o.getDiscountAmount()).totalAmount(o.getTotalAmount())
                .voucherId(o.getVoucher() == null ? null : o.getVoucher().getVoucherId())
                .voucherCode(o.getVoucher() == null ? null : o.getVoucher().getCode())
                .items(items).statusHistories(histories).payments(payments).createdAt(o.getCreatedAt()).updatedAt(o.getUpdatedAt()).build();
    }
}
