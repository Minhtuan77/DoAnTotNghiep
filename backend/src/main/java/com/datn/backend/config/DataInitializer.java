package com.datn.backend.config;

import com.datn.backend.entity.Permission;
import com.datn.backend.entity.Role;
import com.datn.backend.repository.PermissionRepository;
import com.datn.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // =========================
        // 1. Tạo Permission
        // =========================

        Permission productRead =
                createPermission(
                        "PRODUCT_READ",
                        "Xem sản phẩm"
                );

        Permission productCreate =
                createPermission(
                        "PRODUCT_CREATE",
                        "Tạo sản phẩm"
                );

        Permission productUpdate =
                createPermission(
                        "PRODUCT_UPDATE",
                        "Cập nhật sản phẩm"
                );

        Permission productDelete =
                createPermission(
                        "PRODUCT_DELETE",
                        "Xóa sản phẩm"
                );

        Permission userRead =
                createPermission(
                        "USER_READ",
                        "Xem người dùng"
                );

        Permission userUpdate =
                createPermission(
                        "USER_UPDATE",
                        "Cập nhật người dùng"
                );

        Permission userDelete =
                createPermission(
                        "USER_DELETE",
                        "Xóa người dùng"
                );


        // =========================
        // 2. Tạo Role
        // =========================

        Role admin = createRole(
                "ADMIN",
                "Quản trị viên"
        );

        Role staff = createRole(
                "STAFF",
                "Nhân viên"
        );

        Role customer = createRole(
                "CUSTOMER",
                "Khách hàng"
        );


        // =========================
        // 3. Mapping ADMIN
        // =========================

        admin.getPermissions().add(productRead);
        admin.getPermissions().add(productCreate);
        admin.getPermissions().add(productUpdate);
        admin.getPermissions().add(productDelete);

        admin.getPermissions().add(userRead);
        admin.getPermissions().add(userUpdate);
        admin.getPermissions().add(userDelete);


        // =========================
        // 4. Mapping STAFF
        // =========================

        staff.getPermissions().add(productRead);
        staff.getPermissions().add(productUpdate);

        staff.getPermissions().add(userRead);


        // =========================
        // 5. Mapping CUSTOMER
        // =========================

        customer.getPermissions().add(productRead);


        // =========================
        // 6. Save
        // =========================

        roleRepository.save(admin);
        roleRepository.save(staff);
        roleRepository.save(customer);
    }


    private Permission createPermission(
            String code,
            String description
    ) {

        return permissionRepository
                .findByPermissionCode(code)
                .orElseGet(() ->
                        permissionRepository.save(
                                Permission.builder()
                                        .permissionCode(code)
                                        .description(description)
                                        .build()
                        )
                );
    }


    private Role createRole(
            String code,
            String name
    ) {

        return roleRepository
                .findByRoleCode(code)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .roleCode(code)
                                        .roleName(name)
                                        .description(name)
                                        .build()
                        )
                );
    }
}
