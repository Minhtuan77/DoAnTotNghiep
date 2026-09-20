package com.datn.backend.controller;

import com.datn.backend.dto.request.CreatePermissionRequest;
import com.datn.backend.dto.response.PermissionResponse;
import com.datn.backend.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> listPermissions() {

        return ResponseEntity.ok(
                adminRoleService.listPermissions()
        );
    }

    @PostMapping
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
