package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductSpecificationRequest {
    @NotBlank(message = "Tên thông số không được để trống")
    private String specName;
    @NotBlank(message = "Giá trị thông số không được để trống")
    private String specValue;
}