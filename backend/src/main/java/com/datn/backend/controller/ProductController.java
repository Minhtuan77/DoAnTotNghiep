package com.datn.backend.controller;

import com.datn.backend.dto.request.ProductRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.ProductResponse;
import com.datn.backend.entity.enums.ProductStatus;
import com.datn.backend.service.ProductService;
import com.datn.backend.service.BehaviorLogService;
import com.datn.backend.entity.enums.BehaviorActionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.datn.backend.security.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final BehaviorLogService behaviorLogService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId
    ) {
        if (keyword != null && !keyword.isBlank()) {
            behaviorLogService.record(userDetails == null ? null : userDetails.getUserId(), sessionId, null, BehaviorActionType.SEARCH, keyword, null);
        }
        return ResponseEntity.ok(productService.getAllProducts(
                page, size, sortBy, sortDir, keyword, categoryId, brandId, minPrice, maxPrice, ProductStatus.ACTIVE
        ));
    }


    @GetMapping("/management")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsForManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status
    ) {
        return ResponseEntity.ok(productService.getAllProducts(
                page, size, sortBy, sortDir, keyword, categoryId, brandId, minPrice, maxPrice, status
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        ProductResponse response = productService.getProductById(id);
        behaviorLogService.record(userDetails == null ? null : userDetails.getUserId(), sessionId, id, BehaviorActionType.VIEW, null, null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
