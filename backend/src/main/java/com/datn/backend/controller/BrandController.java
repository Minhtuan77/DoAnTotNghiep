package com.datn.backend.controller;

import com.datn.backend.dto.request.BrandRequest;
import com.datn.backend.dto.response.BrandResponse;
import com.datn.backend.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;


    // =========================================================
    // PUBLIC APIs
    // =========================================================

    /**
     * Lấy danh sách thương hiệu.
     * Guest cũng có thể truy cập.
     */
    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {

        return ResponseEntity.ok(
                brandService.getAllBrands()
        );
    }


    /**
     * Lấy chi tiết thương hiệu.
     * Guest cũng có thể truy cập.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                brandService.getBrandById(id)
        );
    }


    // =========================================================
    // MANAGEMENT APIs
    // =========================================================

    /**
     * Tạo thương hiệu.
     *
     * Yêu cầu permission BRAND_CREATE.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody BrandRequest request
    ) {

        BrandResponse response =
                brandService.createBrand(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /**
     * Cập nhật thương hiệu.
     *
     * Yêu cầu permission BRAND_UPDATE.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request
    ) {

        return ResponseEntity.ok(
                brandService.updateBrand(id, request)
        );
    }


    /**
     * Xóa thương hiệu.
     *
     * Chỉ user có BRAND_DELETE mới được thực hiện.
     * Hiện tại permission này được cấp cho ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BRAND_DELETE')")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id
    ) {

        brandService.deleteBrand(id);

        return ResponseEntity.noContent().build();
    }
}