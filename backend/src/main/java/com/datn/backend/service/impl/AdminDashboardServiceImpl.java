package com.datn.backend.service.impl;

import com.datn.backend.dto.response.AdminDashboardResponse;
import com.datn.backend.entity.Inventory;
import com.datn.backend.entity.Order;
import com.datn.backend.entity.enums.OrderStatus;
import com.datn.backend.entity.enums.ProductStatus;
import com.datn.backend.entity.enums.ReviewStatus;
import com.datn.backend.repository.InventoryRepository;
import com.datn.backend.repository.OrderItemRepository;
import com.datn.backend.repository.OrderRepository;
import com.datn.backend.repository.ProductRepository;
import com.datn.backend.repository.ReviewRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public AdminDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDate firstDay = today.minusDays(6);
        LocalDateTime sevenDayStart = firstDay.atStartOfDay();

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        long pendingOrders = 0;
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            statusCounts.put(status.name(), count);
            if (status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PENDING_CONFIRMATION) {
                pendingOrders += count;
            }
        }

        BigDecimal totalRevenue = nvl(orderRepository.sumRevenueByStatuses(List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED)));
        BigDecimal todayRevenue = nvl(orderRepository.sumRevenueBetween(
                List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED), todayStart, tomorrowStart));

        Map<LocalDate, AdminDashboardResponse.RevenuePoint> revenueMap = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = firstDay.plusDays(i);
            revenueMap.put(date, AdminDashboardResponse.RevenuePoint.builder()
                    .date(date)
                    .revenue(BigDecimal.ZERO)
                    .orders(0)
                    .build());
        }
        for (Object[] row : orderRepository.revenueByDay(sevenDayStart, tomorrowStart)) {
            LocalDate date = toLocalDate(row[0]);
            AdminDashboardResponse.RevenuePoint point = revenueMap.get(date);
            if (point != null) {
                point.setRevenue(toBigDecimal(row[1]));
                point.setOrders(((Number) row[2]).longValue());
            }
        }

        List<AdminDashboardResponse.TopProduct> topProducts = new ArrayList<>();
        for (Object[] row : orderItemRepository.findTopSellingProducts(List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED), PageRequest.of(0, 5))) {
            topProducts.add(AdminDashboardResponse.TopProduct.builder()
                    .productId(((Number) row[0]).longValue())
                    .name(String.valueOf(row[1]))
                    .quantitySold(((Number) row[2]).longValue())
                    .revenue(toBigDecimal(row[3]))
                    .build());
        }

        List<AdminDashboardResponse.RecentOrder> recentOrders = orderRepository
                .findTop8ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toRecentOrder)
                .toList();

        List<Inventory> lowStock = inventoryRepository.findLowStockProducts(PageRequest.of(0, 6)).getContent();
        List<AdminDashboardResponse.LowStockItem> lowStockItems = lowStock.stream()
                .map(i -> AdminDashboardResponse.LowStockItem.builder()
                        .productId(i.getProductId())
                        .productName(i.getProduct() == null ? "Sản phẩm #" + i.getProductId() : i.getProduct().getName())
                        .quantityOnHand(i.getQuantityOnHand())
                        .quantityReserved(i.getQuantityReserved())
                        .availableQuantity(Math.max(0, i.getQuantityOnHand() - i.getQuantityReserved()))
                        .lowStockThreshold(i.getLowStockThreshold())
                        .build())
                .toList();

        return AdminDashboardResponse.builder()
                .totalProducts(productRepository.count())
                .activeProducts(productRepository.countByStatus(ProductStatus.ACTIVE))
                .totalOrders(orderRepository.count())
                .totalCustomers(userRepository.countByRole_RoleCode("CUSTOMER"))
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .lowStockProducts(inventoryRepository.countLowStockProducts())
                .pendingReviews(reviewRepository.countByStatus(ReviewStatus.PENDING))
                .todayOrders(orderRepository.countByCreatedAtBetween(todayStart, tomorrowStart))
                .todayRevenue(todayRevenue)
                .orderStatusCounts(statusCounts)
                .revenueLast7Days(new ArrayList<>(revenueMap.values()))
                .topProducts(topProducts)
                .recentOrders(recentOrders)
                .lowStockItems(lowStockItems)
                .build();
    }

    private AdminDashboardResponse.RecentOrder toRecentOrder(Order order) {
        return AdminDashboardResponse.RecentOrder.builder()
                .orderId(order.getOrderId())
                .orderCode(order.getOrderCode())
                .recipientName(order.getRecipientName())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
