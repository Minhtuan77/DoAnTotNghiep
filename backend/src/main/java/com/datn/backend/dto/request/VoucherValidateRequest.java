package com.datn.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VoucherValidateRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;
    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @DecimalMin(value = "0.00", message = "Giá trị đơn hàng không được âm")
    private BigDecimal orderAmount;
}
