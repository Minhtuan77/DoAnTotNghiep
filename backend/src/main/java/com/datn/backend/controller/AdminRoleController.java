package com.datn.backend.controller;

import com.datn.backend.dto.request.AssignPermissionsRequest;
import com.datn.backend.dto.response.RoleResponse;
import com.datn.backend.service.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping
    public ResponseEntity<List<RoleResponse>> listRoles() {

        return ResponseEntity.ok(
                adminRoleService.listRoles()
        );
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable Integer roleId
    ) {

        return ResponseEntity.ok(
                adminRoleService.getRole(roleId)
        );
    }

    @PostMapping("/{roleId}/permissions")
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
