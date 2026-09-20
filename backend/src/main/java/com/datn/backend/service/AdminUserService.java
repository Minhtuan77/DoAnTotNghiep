package com.datn.backend.service;

import com.datn.backend.dto.request.CreateStaffRequest;
import com.datn.backend.dto.request.UpdateUserRoleRequest;
import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    // keyword: tìm theo fullName/email/phone (LIKE, không phân biệt hoa thường)
    // roleCode, status: lọc chính xác, truyền null để bỏ qua điều kiện
    PageResponse<UserResponse> listUsers(
            String keyword,
            String roleCode,
            UserStatus status,
            Pageable pageable
    );

    UserResponse getUserById(Long userId);

    // Tạo trực tiếp tài khoản STAFF hoặc ADMIN (bỏ qua bước đăng ký công khai)
    UserResponse createStaffOrAdmin(CreateStaffRequest request);

    // actingAdminId: id của admin đang thực hiện thao tác, dùng để chặn tự khóa chính mình
    UserResponse updateUserStatus(
            Long targetUserId,
            UpdateUserStatusRequest request,
            Long actingAdminId
    );

    UserResponse updateUserRole(
            Long targetUserId,
            UpdateUserRoleRequest request,
            Long actingAdminId
    );
}
