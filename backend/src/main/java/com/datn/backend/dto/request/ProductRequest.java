package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU không được để trống")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá gốc không được để trống")
    @DecimalMin(value = "0.0", message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Giá khuyến mãi phải lớn hơn hoặc bằng 0")
    private BigDecimal salePrice;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    // Chỉ dùng khi TẠO sản phẩm để khởi tạo tồn kho.
    // Khi UPDATE sản phẩm, tồn kho phải cập nhật qua Inventory API.

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;

    private Integer brandId;

    private ProductStatus status;

    private List<String> imageUrls;

    @Valid
    private List<ProductSpecificationRequest> specifications;
}