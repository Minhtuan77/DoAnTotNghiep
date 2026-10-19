package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long totalCustomers;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long lowStockProducts;
    private long pendingReviews;
    private long todayOrders;
    private BigDecimal todayRevenue;
    private Map<String, Long> orderStatusCounts;
    private List<RevenuePoint> revenueLast7Days;
    private List<TopProduct> topProducts;
    private List<RecentOrder> recentOrders;
    private List<LowStockItem> lowStockItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePoint {
        private LocalDate date;
        private BigDecimal revenue;
        private long orders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String name;
        private long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private Long orderId;
        private String orderCode;
        private String recipientName;
        private String status;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long productId;
        private String productName;
        private Integer quantityOnHand;
        private Integer quantityReserved;
        private Integer availableQuantity;
        private Integer lowStockThreshold;
    }
}
