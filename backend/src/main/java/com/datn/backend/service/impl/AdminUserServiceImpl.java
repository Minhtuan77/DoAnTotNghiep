package com.datn.backend.service.impl;

import com.datn.backend.dto.request.CreateStaffRequest;
import com.datn.backend.dto.request.UpdateUserRoleRequest;
import com.datn.backend.dto.request.UpdateUserStatusRequest;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.Role;
import com.datn.backend.entity.User;
import com.datn.backend.entity.UserSession;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.exception.EmailAlreadyExistsException;
import com.datn.backend.exception.InvalidRoleAssignmentException;
import com.datn.backend.exception.PhoneAlreadyExistsException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.exception.RoleNotFoundException;
import com.datn.backend.exception.SelfActionNotAllowedException;
import com.datn.backend.repository.RoleRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.repository.UserSessionRepository;
import com.datn.backend.repository.spec.UserSpecification;
import com.datn.backend.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    // Endpoint tạo Staff/Admin (CreateStaffRequest.roleCode) chỉ chấp
    // nhận 2 role này. Muốn tạo CUSTOMER phải đi qua /api/auth/register.
    private static final Set<String> ASSIGNABLE_ROLES_ON_CREATE =
            Set.of("STAFF", "ADMIN");

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserSessionRepository userSessionRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(
            String keyword,
            String roleCode,
            UserStatus status,
            Pageable pageable
    ) {

        Page<User> page =
                userRepository.findAll(
                        UserSpecification.filter(
                                keyword,
                                roleCode,
                                status
                        ),
                        pageable
                );

        return PageResponse.from(
                page,
                this::toUserResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {

        return toUserResponse(
                findUser(userId)
        );
    }

    @Override
    public UserResponse createStaffOrAdmin(
            CreateStaffRequest request
    ) {

        String roleCode =
                request.getRoleCode()
                        .trim()
                        .toUpperCase();

        if (!ASSIGNABLE_ROLES_ON_CREATE.contains(roleCode)) {

            throw new InvalidRoleAssignmentException(
                    "roleCode chỉ được phép là STAFF hoặc ADMIN "
                            + "(tạo CUSTOMER phải dùng /api/auth/register)"
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String phone = request.getPhone();

        if (phone != null && !phone.isBlank()) {

            phone = phone.trim();

            if (userRepository.existsByPhone(phone)) {
                throw new PhoneAlreadyExistsException(phone);
            }
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Role role =
                roleRepository
                        .findByRoleCode(roleCode)
                        .orElseThrow(() ->
                                new RoleNotFoundException(roleCode)
                        );

        User user =
                User.builder()
                        .fullName(request.getFullName().trim())
                        .email(email)
                        .phone(phone)
                        .passwordHash(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )
                        .role(role)
                        // Admin tạo trực tiếp -> bỏ qua bước xác thực email
                        .status(UserStatus.ACTIVE)
                        .build();

        user = userRepository.save(user);

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateUserStatus(
            Long targetUserId,
            UpdateUserStatusRequest request,
            Long actingAdminId
    ) {

        if (targetUserId.equals(actingAdminId)) {

            throw new SelfActionNotAllowedException(
                    "Không thể tự thay đổi trạng thái tài khoản của chính mình"
            );
        }

        User user = findUser(targetUserId);

        user.setStatus(request.getStatus());

        userRepository.save(user);

        // Khóa/tạm ngưng tài khoản thì phải thu hồi mọi session đang
        // hiệu lực, tránh access token cũ (đang còn hạn) vẫn dùng được.
        if (request.getStatus() != UserStatus.ACTIVE) {
            revokeAllActiveSessions(user);
        }

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateUserRole(
            Long targetUserId,
            UpdateUserRoleRequest request,
            Long actingAdminId
    ) {

        if (targetUserId.equals(actingAdminId)) {

            throw new SelfActionNotAllowedException(
                    "Không thể tự đổi role của chính mình"
            );
        }

        User user = findUser(targetUserId);

        String roleCode =
                request.getRoleCode()
                        .trim()
                        .toUpperCase();

        Role role =
                roleRepository
                        .findByRoleCode(roleCode)
                        .orElseThrow(() ->
                                new RoleNotFoundException(roleCode)
                        );

        user.setRole(role);

        userRepository.save(user);

        // Role đổi -> quyền hạn (authorities) đổi, nhưng access token cũ
        // vẫn còn mang role/permission cũ cho tới khi hết hạn -> ép logout.
        revokeAllActiveSessions(user);

        return toUserResponse(user);
    }

    private void revokeAllActiveSessions(User user) {

        List<UserSession> sessions =
                userSessionRepository
                        .findByUser_UserIdAndRevokedAtIsNull(
                                user.getUserId()
                        );

        LocalDateTime now = LocalDateTime.now();

        sessions.forEach(
                session -> session.setRevokedAt(now)
        );

        userSessionRepository.saveAll(sessions);
    }

    private User findUser(Long userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy người dùng với ID: " + userId
                        )
                );
    }

    private UserResponse toUserResponse(User user) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roleCode(user.getRole().getRoleCode())
                .roleName(user.getRole().getRoleName())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
