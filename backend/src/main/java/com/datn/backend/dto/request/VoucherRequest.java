package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.DiscountType;
import com.datn.backend.entity.enums.VoucherStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    private String code;
    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;
    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;
    @DecimalMin(value = "0.00", message = "Giá trị đơn tối thiểu không được âm")
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    @DecimalMin(value = "0.00", inclusive = false, message = "Mức giảm tối đa phải lớn hơn 0")
    private BigDecimal maxDiscountValue;
    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    private Integer usageLimit;
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startDate;
    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endDate;
    private VoucherStatus status = VoucherStatus.ACTIVE;
}
