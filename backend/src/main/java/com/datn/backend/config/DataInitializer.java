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

    // Tài khoản admin mặc định (chỉ được tạo nếu DB chưa có admin nào).
    // BẮT BUỘC đổi mật khẩu này ngay sau lần đăng nhập đầu tiên khi deploy thật.
    private static final String DEFAULT_ADMIN_EMAIL = "admin@datn.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // @Transactional bắt buộc phải có ở đây: từ lần chạy thứ 2 trở đi,
    // findByRoleCode() trả về Role đã tồn tại trong DB, khi đó
    // role.getPermissions() là lazy collection của Hibernate. Gọi .add()
    // lên nó ngoài phạm vi 1 transaction/session sẽ ném
    // LazyInitializationException và làm ứng dụng crash lúc khởi động.
    @Override
    @Transactional
    public void run(String... args) {

        // =========================
        // 1. Tạo Permission
        // =========================
        // Danh sách permission dưới đây khớp với bảng RBAC thiết kế cho
        // CUSTOMER / STAFF / ADMIN. ORDER_* và INVENTORY_* được seed sẵn dù
        // Module Order/Inventory chưa code, để khi làm tới module đó chỉ cần
        // gắn @PreAuthorize mà không phải sửa lại DataInitializer.
        //
        // Ghi chú: bảng thiết kế không có USER_CREATE / ROLE_CREATE /
        // PERMISSION_CREATE riêng -> hành động "tạo" ở 3 nhóm tài nguyên này
        // dùng chung permission *_UPDATE (xem AdminUserController,
        // AdminRoleController, AdminPermissionController).

        Permission productRead =
                createPermission("PRODUCT_READ", "Xem sản phẩm");
        Permission productCreate =
                createPermission("PRODUCT_CREATE", "Tạo sản phẩm");
        Permission productUpdate =
                createPermission("PRODUCT_UPDATE", "Cập nhật sản phẩm");
        Permission productDelete =
                createPermission("PRODUCT_DELETE", "Xóa sản phẩm");

        Permission orderRead =
                createPermission("ORDER_READ", "Xem đơn hàng");
        Permission orderUpdate =
                createPermission("ORDER_UPDATE", "Cập nhật đơn hàng");
        Permission orderDelete =
                createPermission("ORDER_DELETE", "Xóa/hủy đơn hàng");

        Permission userRead =
                createPermission("USER_READ", "Xem người dùng");
        Permission userUpdate =
                createPermission("USER_UPDATE", "Cập nhật người dùng");
        Permission userDelete =
                createPermission("USER_DELETE", "Xóa người dùng");

        Permission roleRead =
                createPermission("ROLE_READ", "Xem role");
        Permission roleUpdate =
                createPermission("ROLE_UPDATE", "Cập nhật role/gán quyền");

        Permission permissionRead =
                createPermission("PERMISSION_READ", "Xem permission");
        Permission permissionUpdate =
                createPermission("PERMISSION_UPDATE", "Tạo/cập nhật permission");

        Permission inventoryRead =
                createPermission("INVENTORY_READ", "Xem tồn kho");
        Permission inventoryUpdate =
                createPermission("INVENTORY_UPDATE", "Cập nhật tồn kho");


        // =========================
        // 2. Tạo Role
        // =========================

        Role admin = createRole("ADMIN", "Quản trị viên");
        Role staff = createRole("STAFF", "Nhân viên");
        Role customer = createRole("CUSTOMER", "Khách hàng");


        // =========================
        // 3. Mapping ADMIN - toàn quyền
        // =========================

        admin.getPermissions().add(productRead);
        admin.getPermissions().add(productCreate);
        admin.getPermissions().add(productUpdate);
        admin.getPermissions().add(productDelete);

        admin.getPermissions().add(orderRead);
        admin.getPermissions().add(orderUpdate);
        admin.getPermissions().add(orderDelete);

        admin.getPermissions().add(userRead);
        admin.getPermissions().add(userUpdate);
        admin.getPermissions().add(userDelete);

        admin.getPermissions().add(roleRead);
        admin.getPermissions().add(roleUpdate);

        admin.getPermissions().add(permissionRead);
        admin.getPermissions().add(permissionUpdate);

        admin.getPermissions().add(inventoryRead);
        admin.getPermissions().add(inventoryUpdate);


        // =========================
        // 4. Mapping STAFF - vận hành, không quản trị hệ thống
        // =========================

        staff.getPermissions().add(productRead);
        staff.getPermissions().add(productUpdate);

        staff.getPermissions().add(orderRead);
        staff.getPermissions().add(orderUpdate);

        staff.getPermissions().add(userRead);

        staff.getPermissions().add(inventoryRead);
        staff.getPermissions().add(inventoryUpdate);


        // =========================
        // 5. Mapping CUSTOMER
        // =========================
        // Các hành động của khách hàng (xem sản phẩm, đặt hàng, đánh giá...)
        // là API công khai/theo quyền sở hữu dữ liệu của chính họ, không cần
        // permission code riêng - không gán permission nào ở đây.


        // =========================
        // 6. Save
        // =========================

        admin = roleRepository.save(admin);
        roleRepository.save(staff);
        roleRepository.save(customer);


        // =========================
        // 7. Seed tài khoản Admin mặc định
        // =========================
        seedDefaultAdmin(admin);
    }


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
