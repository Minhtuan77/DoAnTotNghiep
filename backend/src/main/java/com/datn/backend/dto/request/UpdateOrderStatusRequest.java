package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
    @Size(max = 255)
    private String note;
}
