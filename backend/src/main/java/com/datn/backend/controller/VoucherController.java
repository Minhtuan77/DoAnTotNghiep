package com.datn.backend.controller;

import com.datn.backend.dto.request.VoucherRequest;
import com.datn.backend.dto.request.VoucherValidateRequest;
import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.VoucherResponse;
import com.datn.backend.dto.response.VoucherValidationResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<VoucherValidationResponse>> validate(@AuthenticationPrincipal CustomUserDetails user,
                                                                           @Valid @RequestBody VoucherValidateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra voucher thành công",
                voucherService.validate(request.getCode(), request.getOrderAmount(), user.getUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VOUCHER_READ')")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAll(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_READ')")
    public ResponseEntity<ApiResponse<VoucherResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VOUCHER_CREATE')")
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo voucher thành công", voucherService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_UPDATE')")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(@PathVariable Long id, @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật voucher thành công", voucherService.update(id, request)));
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('VOUCHER_UPDATE')")
    public ResponseEntity<ApiResponse<VoucherResponse>> disable(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa voucher thành công", voucherService.disable(id)));
    }
}
