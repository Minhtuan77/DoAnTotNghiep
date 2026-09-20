package com.datn.backend.service.impl;

import com.datn.backend.dto.request.AssignPermissionsRequest;
import com.datn.backend.dto.request.CreatePermissionRequest;
import com.datn.backend.dto.response.PermissionResponse;
import com.datn.backend.dto.response.RoleResponse;
import com.datn.backend.entity.Permission;
import com.datn.backend.entity.Role;
import com.datn.backend.exception.PermissionCodeAlreadyExistsException;
import com.datn.backend.exception.PermissionNotFoundException;
import com.datn.backend.exception.RoleNotFoundException;
import com.datn.backend.repository.PermissionRepository;
import com.datn.backend.repository.RoleRepository;
import com.datn.backend.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminRoleServiceImpl implements AdminRoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(Integer roleId) {

        return toRoleResponse(
                findRole(roleId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {

        return permissionRepository.findAll()
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    @Override
    public PermissionResponse createPermission(
            CreatePermissionRequest request
    ) {

        String code = request.getPermissionCode().trim();

        if (permissionRepository.existsByPermissionCode(code)) {
            throw new PermissionCodeAlreadyExistsException(code);
        }

        Permission permission =
                Permission.builder()
                        .permissionCode(code)
                        .description(request.getDescription())
                        .build();

        permission = permissionRepository.save(permission);

        return toPermissionResponse(permission);
    }

    @Override
    public RoleResponse assignPermissionsToRole(
            Integer roleId,
            AssignPermissionsRequest request
    ) {

        Role role = findRole(roleId);

        List<Permission> permissions =
                permissionRepository.findAllById(
                        request.getPermissionIds()
                );

        // Nếu id nào trong request không tồn tại trong DB -> báo lỗi rõ ràng
        // thay vì âm thầm bỏ qua.
        if (permissions.size() != request.getPermissionIds().size()) {

            List<Integer> foundIds =
                    permissions.stream()
                            .map(Permission::getPermissionId)
                            .toList();

            Integer missingId =
                    request.getPermissionIds()
                            .stream()
                            .filter(id -> !foundIds.contains(id))
                            .findFirst()
                            .orElse(null);

            throw new PermissionNotFoundException(missingId);
        }

        role.getPermissions().addAll(permissions);

        role = roleRepository.save(role);

        return toRoleResponse(role);
    }

    @Override
    public RoleResponse removePermissionFromRole(
            Integer roleId,
            Integer permissionId
    ) {

        Role role = findRole(roleId);

        boolean removed =
                role.getPermissions().removeIf(
                        p -> p.getPermissionId().equals(permissionId)
                );

        if (!removed) {
            throw new PermissionNotFoundException(permissionId);
        }

        role = roleRepository.save(role);

        return toRoleResponse(role);
    }

    private Role findRole(Integer roleId) {

        return roleRepository
                .findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "id=" + roleId
                        )
                );
    }

    private RoleResponse toRoleResponse(Role role) {

        List<PermissionResponse> permissions =
                role.getPermissions()
                        .stream()
                        .map(this::toPermissionResponse)
                        .toList();

        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .permissions(permissions)
                .build();
    }

    private PermissionResponse toPermissionResponse(
            Permission permission
    ) {

        return PermissionResponse.builder()
                .permissionId(permission.getPermissionId())
                .permissionCode(permission.getPermissionCode())
                .description(permission.getDescription())
                .build();
    }
}
