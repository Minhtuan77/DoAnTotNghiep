package com.datn.backend.service;

import com.datn.backend.dto.request.ProductRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ProductResponse;
import com.datn.backend.entity.enums.ProductStatus;

import java.math.BigDecimal;

public interface ProductService {
    PageResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy, String sortDir,
            String keyword, Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status
    );

    ProductResponse getProductById(Long id);
    ProductResponse getProductBySlug(String slug);
    ProductResponse createProduct(ProductRequest request, Long userId);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
