package com.datn.backend.dto.response;

import com.datn.backend.entity.enums.DiscountType;
import com.datn.backend.entity.enums.VoucherStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private Long voucherId;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer remainingUses;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;
    private LocalDateTime createdAt;
}
