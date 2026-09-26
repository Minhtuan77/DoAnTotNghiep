package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateOrderRequest {
    @NotBlank @Size(max = 150)
    private String recipientName;

    @NotBlank @Pattern(regexp = "^[0-9+ .()-]{8,20}$", message = "Số điện thoại không hợp lệ")
    private String recipientPhone;

    @NotBlank @Size(max = 500)
    private String shippingAddress;

    @Size(max = 500)
    private String note;

    @NotNull
    private PaymentMethod paymentMethod;

    private String voucherCode;

    /** Bắt buộc khi paymentMethod = ONLINE, ví dụ VNPAY. */
    @Size(max = 50)
    private String paymentProvider;
}
