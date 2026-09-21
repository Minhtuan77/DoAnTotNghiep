package com.datn.backend.controller;

import com.datn.backend.dto.request.AssignPermissionsRequest;
import com.datn.backend.dto.response.RoleResponse;
import com.datn.backend.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// "/api/admin/**" đã bị giới hạn ROLE_ADMIN ở SecurityConfig (lớp phòng
// thủ 1); @PreAuthorize dưới đây là lớp phòng thủ 2 theo permission chi
// tiết (ROLE_READ / ROLE_UPDATE), xem giải thích ở AdminUserController.
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<RoleResponse>> listRoles() {

        return ResponseEntity.ok(
                adminRoleService.listRoles()
        );
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable Integer roleId
    ) {

        return ResponseEntity.ok(
                adminRoleService.getRole(roleId)
        );
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleResponse> assignPermissions(
            @PathVariable Integer roleId,

            @Valid @RequestBody AssignPermissionsRequest request
    ) {

        return ResponseEntity.ok(
                adminRoleService.assignPermissionsToRole(
                        roleId,
                        request
                )
        );
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleResponse> removePermission(
            @PathVariable Integer roleId,

            @PathVariable Integer permissionId
    ) {

        return ResponseEntity.ok(
                adminRoleService.removePermissionFromRole(
                        roleId,
                        permissionId
                )
        );
    }
}
