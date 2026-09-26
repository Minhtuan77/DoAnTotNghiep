package com.datn.backend.config;

import com.datn.backend.entity.Permission;
import com.datn.backend.entity.Role;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.repository.PermissionRepository;
import com.datn.backend.repository.RoleRepository;
import com.datn.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@datn.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // =====================================================
        // 1. PRODUCT PERMISSIONS
        // =====================================================

        Permission productRead =
                createPermission("PRODUCT_READ", "Xem sản phẩm");

        Permission productCreate =
                createPermission("PRODUCT_CREATE", "Tạo sản phẩm");

        Permission productUpdate =
                createPermission("PRODUCT_UPDATE", "Cập nhật sản phẩm");

        Permission productDelete =
                createPermission("PRODUCT_DELETE", "Xóa sản phẩm");


        // =====================================================
        // 2. CATEGORY PERMISSIONS
        // =====================================================

        Permission categoryRead =
                createPermission("CATEGORY_READ", "Xem danh mục");

        Permission categoryCreate =
                createPermission("CATEGORY_CREATE", "Tạo danh mục");

        Permission categoryUpdate =
                createPermission("CATEGORY_UPDATE", "Cập nhật danh mục");

        Permission categoryDelete =
                createPermission("CATEGORY_DELETE", "Xóa danh mục");


        // =====================================================
        // 3. BRAND PERMISSIONS
        // =====================================================

        Permission brandRead =
                createPermission("BRAND_READ", "Xem thương hiệu");

        Permission brandCreate =
                createPermission("BRAND_CREATE", "Tạo thương hiệu");

        Permission brandUpdate =
                createPermission("BRAND_UPDATE", "Cập nhật thương hiệu");

        Permission brandDelete =
                createPermission("BRAND_DELETE", "Xóa thương hiệu");


        // =====================================================
        // 4. ORDER PERMISSIONS
        // =====================================================

        Permission orderRead =
                createPermission("ORDER_READ", "Xem đơn hàng");

        Permission orderUpdate =
                createPermission("ORDER_UPDATE", "Cập nhật đơn hàng");

        Permission orderDelete =
                createPermission("ORDER_DELETE", "Xóa/hủy đơn hàng");


        // =====================================================
        // 5. USER PERMISSIONS
        // =====================================================

        Permission userRead =
                createPermission("USER_READ", "Xem người dùng");

        Permission userUpdate =
                createPermission("USER_UPDATE", "Cập nhật người dùng");

        Permission userDelete =
                createPermission("USER_DELETE", "Xóa người dùng");


        // =====================================================
        // 6. ROLE PERMISSIONS
        // =====================================================

        Permission roleRead =
                createPermission("ROLE_READ", "Xem role");

        Permission roleUpdate =
                createPermission("ROLE_UPDATE", "Cập nhật role/gán quyền");


        // =====================================================
        // 7. PERMISSION MANAGEMENT
        // =====================================================

        Permission permissionRead =
                createPermission("PERMISSION_READ", "Xem permission");

        Permission permissionUpdate =
                createPermission(
                        "PERMISSION_UPDATE",
                        "Tạo/cập nhật permission"
                );


        // =====================================================
        // 8. INVENTORY PERMISSIONS
        // =====================================================

        Permission inventoryRead =
                createPermission("INVENTORY_READ", "Xem tồn kho");

        Permission inventoryUpdate =
                createPermission("INVENTORY_UPDATE", "Cập nhật tồn kho");


        // =====================================================
        // VOUCHER PERMISSIONS
        // =====================================================

        Permission voucherRead =
                createPermission("VOUCHER_READ", "Xem voucher");

        Permission voucherCreate =
                createPermission("VOUCHER_CREATE", "Tạo voucher");

        Permission voucherUpdate =
                createPermission("VOUCHER_UPDATE", "Cập nhật/vô hiệu hóa voucher");


        // =====================================================
        // 9. CREATE ROLES
        // =====================================================

        Role admin =
                createRole("ADMIN", "Quản trị viên");

        Role staff =
                createRole("STAFF", "Nhân viên");

        Role customer =
                createRole("CUSTOMER", "Khách hàng");


        // =====================================================
        // 10. ADMIN - TOÀN QUYỀN
        // =====================================================

        // Product
        admin.getPermissions().add(productRead);
        admin.getPermissions().add(productCreate);
        admin.getPermissions().add(productUpdate);
        admin.getPermissions().add(productDelete);

        // Category
        admin.getPermissions().add(categoryRead);
        admin.getPermissions().add(categoryCreate);
        admin.getPermissions().add(categoryUpdate);
        admin.getPermissions().add(categoryDelete);

        // Brand
        admin.getPermissions().add(brandRead);
        admin.getPermissions().add(brandCreate);
        admin.getPermissions().add(brandUpdate);
        admin.getPermissions().add(brandDelete);

        // Order
        admin.getPermissions().add(orderRead);
        admin.getPermissions().add(orderUpdate);
        admin.getPermissions().add(orderDelete);

        // User
        admin.getPermissions().add(userRead);
        admin.getPermissions().add(userUpdate);
        admin.getPermissions().add(userDelete);

        // Role
        admin.getPermissions().add(roleRead);
        admin.getPermissions().add(roleUpdate);

        // Permission
        admin.getPermissions().add(permissionRead);
        admin.getPermissions().add(permissionUpdate);

        // Inventory
        admin.getPermissions().add(inventoryRead);
        admin.getPermissions().add(inventoryUpdate);

        // Voucher
        admin.getPermissions().add(voucherRead);
        admin.getPermissions().add(voucherCreate);
        admin.getPermissions().add(voucherUpdate);


        // =====================================================
        // 11. STAFF - QUẢN LÝ VẬN HÀNH
        // =====================================================

        // Product
        staff.getPermissions().add(productRead);
        staff.getPermissions().add(productCreate);
        staff.getPermissions().add(productUpdate);

        // Category
        staff.getPermissions().add(categoryRead);
        staff.getPermissions().add(categoryCreate);
        staff.getPermissions().add(categoryUpdate);

        // Brand
        staff.getPermissions().add(brandRead);
        staff.getPermissions().add(brandCreate);
        staff.getPermissions().add(brandUpdate);

        // Order
        staff.getPermissions().add(orderRead);
        staff.getPermissions().add(orderUpdate);

        // User
        staff.getPermissions().add(userRead);

        // Inventory
        staff.getPermissions().add(inventoryRead);
        staff.getPermissions().add(inventoryUpdate);


        // =====================================================
        // 12. CUSTOMER
        // =====================================================
        /*
         * CUSTOMER không cần permission quản trị.
         *
         * Các API như:
         * - xem sản phẩm
         * - xem category
         * - xem brand
         *
         * được permitAll trong SecurityConfig.
         *
         * Các chức năng Cart / Order / Wishlist / Review sau này
         * sẽ kiểm tra quyền sở hữu dữ liệu của chính user.
         */


        // =====================================================
        // 13. SAVE ROLES
        // =====================================================

        admin = roleRepository.save(admin);

        roleRepository.save(staff);

        roleRepository.save(customer);


        // =====================================================
        // 14. CREATE DEFAULT ADMIN
        // =====================================================

        seedDefaultAdmin(admin);
    }


    // =========================================================
    // DEFAULT ADMIN
    // =========================================================

    private void seedDefaultAdmin(Role adminRole) {

        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            return;
        }

        User admin =
                User.builder()
                        .fullName("System Administrator")
                        .email(DEFAULT_ADMIN_EMAIL)
                        .passwordHash(
                                passwordEncoder.encode(
                                        DEFAULT_ADMIN_PASSWORD
                                )
                        )
                        .role(adminRole)
                        .status(UserStatus.ACTIVE)
                        .build();

        userRepository.save(admin);

        System.out.println(
                "==================================================\n" +
                "  Đã tạo tài khoản Admin mặc định:\n" +
                "  Email:    " + DEFAULT_ADMIN_EMAIL + "\n" +
                "  Password: " + DEFAULT_ADMIN_PASSWORD + "\n" +
                "  HÃY ĐỔI MẬT KHẨU NÀY SAU LẦN ĐĂNG NHẬP ĐẦU TIÊN.\n" +
                "=================================================="
        );
    }


    // =========================================================
    // CREATE PERMISSION
    // =========================================================

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


    // =========================================================
    // CREATE ROLE
    // =========================================================

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