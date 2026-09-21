package com.datn.backend.controller;

import com.datn.backend.dto.request.CreatePermissionRequest;
import com.datn.backend.dto.response.PermissionResponse;
import com.datn.backend.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// "/api/admin/**" đã bị giới hạn ROLE_ADMIN ở SecurityConfig (lớp phòng
// thủ 1); @PreAuthorize dưới đây là lớp phòng thủ 2 theo permission chi
// tiết (PERMISSION_READ / PERMISSION_UPDATE).
@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<List<PermissionResponse>> listPermissions() {

        return ResponseEntity.ok(
                adminRoleService.listPermissions()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminRoleService.createPermission(request)
                );
    }
}
