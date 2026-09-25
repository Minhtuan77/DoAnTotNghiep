package com.datn.backend.controller;

import com.datn.backend.dto.request.CategoryRequest;
import com.datn.backend.dto.response.CategoryResponse;
import com.datn.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    // =========================================================
    // PUBLIC APIs
    // =========================================================

    /**
     * Lấy danh sách danh mục.
     * Guest cũng có thể truy cập.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }


    /**
     * Lấy chi tiết danh mục.
     * Guest cũng có thể truy cập.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );
    }


    // =========================================================
    // MANAGEMENT APIs
    // =========================================================

    /**
     * Tạo danh mục.
     *
     * Yêu cầu permission CATEGORY_CREATE.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {

        CategoryResponse response =
                categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /**
     * Cập nhật danh mục.
     *
     * Yêu cầu permission CATEGORY_UPDATE.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {

        return ResponseEntity.ok(
                categoryService.updateCategory(id, request)
        );
    }


    /**
     * Xóa danh mục.
     *
     * Chỉ user có CATEGORY_DELETE mới được thực hiện.
     * Hiện tại permission này được cấp cho ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id
    ) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}