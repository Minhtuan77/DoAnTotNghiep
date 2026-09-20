package com.datn.backend.service;

import com.datn.backend.dto.request.AssignPermissionsRequest;
import com.datn.backend.dto.request.CreatePermissionRequest;
import com.datn.backend.dto.response.PermissionResponse;
import com.datn.backend.dto.response.RoleResponse;

import java.util.List;

public interface AdminRoleService {

    List<RoleResponse> listRoles();

    RoleResponse getRole(Integer roleId);

    List<PermissionResponse> listPermissions();

    PermissionResponse createPermission(CreatePermissionRequest request);

    // Gộp thêm (union) các permissionIds vào role, KHÔNG xóa permission cũ.
    // Dùng endpoint xóa riêng (removePermissionFromRole) để bỏ bớt.
    RoleResponse assignPermissionsToRole(
            Integer roleId,
            AssignPermissionsRequest request
    );

    RoleResponse removePermissionFromRole(
            Integer roleId,
            Integer permissionId
    );
}
