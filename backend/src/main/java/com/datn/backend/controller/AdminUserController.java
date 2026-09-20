package com.datn.backend.controller;

import com.datn.backend.dto.request.CreateStaffRequest;
import com.datn.backend.dto.request.UpdateUserRoleRequest;
import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Toàn bộ "/api/admin/**" đã được giới hạn chỉ ROLE_ADMIN mới gọi được,
// xem SecurityConfig.securityFilterChain() - không cần thêm @PreAuthorize
// ở từng method nữa.
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("createdAt").descending()
                );

        return ResponseEntity.ok(
                adminUserService.listUsers(
                        keyword,
                        roleCode,
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                adminUserService.getUserById(userId)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> createStaffOrAdmin(
            @Valid @RequestBody CreateStaffRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminUserService.createStaffOrAdmin(request)
                );
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long userId,

            @Valid @RequestBody UpdateUserStatusRequest request,

            @AuthenticationPrincipal CustomUserDetails adminDetails
    ) {

        return ResponseEntity.ok(
                adminUserService.updateUserStatus(
                        userId,
                        request,
                        adminDetails.getUserId()
                )
        );
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long userId,

            @Valid @RequestBody UpdateUserRoleRequest request,

            @AuthenticationPrincipal CustomUserDetails adminDetails
    ) {

        return ResponseEntity.ok(
                adminUserService.updateUserRole(
                        userId,
                        request,
                        adminDetails.getUserId()
                )
        );
    }
}
