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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

// Lớp phòng thủ 1: "/api/admin/**" đã được giới hạn chỉ ROLE_ADMIN mới
// gọi được, xem SecurityConfig.securityFilterChain().
// Lớp phòng thủ 2: @PreAuthorize theo permission chi tiết dưới đây - hiện
// tại ADMIN luôn có đủ mọi permission nên 2 lớp này trùng kết quả, nhưng
// permission-based check là điểm mở rộng cho sau này (vd: role MODERATOR
// chỉ có USER_READ mà không có USER_UPDATE, không cần sửa SecurityConfig).
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
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
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                adminUserService.getUserById(userId)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_UPDATE')")
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
    @PreAuthorize("hasAuthority('USER_UPDATE')")
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
    @PreAuthorize("hasAuthority('USER_UPDATE')")
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
