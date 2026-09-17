package com.datn.backend.service.impl;

import com.datn.backend.dto.request.ForgotPasswordRequest;
import com.datn.backend.dto.request.LoginRequest;
import com.datn.backend.dto.request.RegisterRequest;
import com.datn.backend.dto.request.ResetPasswordRequest;
import com.datn.backend.dto.response.AuthResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.PasswordResetToken;
import com.datn.backend.entity.Role;
import com.datn.backend.entity.User;
import com.datn.backend.entity.UserSession;
import com.datn.backend.entity.enums.UserStatus;
import com.datn.backend.exception.AccountNotActiveException;
import com.datn.backend.exception.EmailAlreadyExistsException;
import com.datn.backend.exception.InvalidCredentialsException;
import com.datn.backend.exception.InvalidTokenException;
import com.datn.backend.exception.PhoneAlreadyExistsException;
import com.datn.backend.repository.PasswordResetTokenRepository;
import com.datn.backend.repository.RoleRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.repository.UserSessionRepository;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.security.JwtUtil;
import com.datn.backend.service.AuthService;
import com.datn.backend.util.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserSessionRepository userSessionRepository;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final TokenHashUtil tokenHashUtil;

    @Override
    public UserResponse register(
            RegisterRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String phone =
                request.getPhone();

        if (phone != null
                && !phone.isBlank()) {

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
                        .findByRoleCode(CUSTOMER_ROLE)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Chưa cấu hình role CUSTOMER"
                                )
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
                        .status(UserStatus.ACTIVE)
                        .build();

        user = userRepository.save(user);

        return toUserResponse(user);
    }

    @Override
    public AuthResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (user.getStatus() != UserStatus.ACTIVE) {

            throw new AccountNotActiveException(
                    user.getStatus().name()
            );
        }

        Authentication authentication;

        try {

            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    request.getPassword()
                            )
                    );

        } catch (BadCredentialsException e) {

            throw new InvalidCredentialsException();
        }

        CustomUserDetails userDetails =
                (CustomUserDetails)
                        authentication.getPrincipal();

        String accessToken =
                jwtUtil.generateAccessToken(
                        userDetails
                );

        String refreshToken =
                jwtUtil.generateRefreshToken(
                        userDetails
                );

        saveSession(
                user,
                refreshToken,
                httpRequest
        );

        user.setLastLoginAt(
                LocalDateTime.now()
        );

        userRepository.save(user);

        return buildAuthResponse(
                accessToken,
                refreshToken,
                userDetails
        );
    }

    @Override
    public AuthResponse refreshToken(
            String refreshToken,
            HttpServletRequest httpRequest
    ) {

        if (refreshToken == null
                || refreshToken.isBlank()) {

            throw new InvalidTokenException(
                    "Refresh token không được để trống"
            );
        }

        try {

            String type =
                    jwtUtil.extractTokenType(
                            refreshToken
                    );

            if (!"REFRESH".equals(type)) {

                throw new InvalidTokenException(
                        "Token không phải refresh token"
                );
            }

            Long userId =
                    jwtUtil.extractUserId(
                            refreshToken
                    );

            String email =
                    jwtUtil.extractEmail(
                            refreshToken
                    );

            String tokenHash =
                    tokenHashUtil.hash(
                            refreshToken
                    );

            UserSession session =
                    userSessionRepository
                            .findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                                    tokenHash,
                                    LocalDateTime.now()
                            )
                            .orElseThrow(() ->
                                    new InvalidTokenException(
                                            "Refresh token không hợp lệ hoặc đã hết hạn"
                                    )
                            );

            User user = session.getUser();

            if (!user.getUserId().equals(userId)
                    || !user.getEmail().equals(email)) {

                throw new InvalidTokenException(
                        "Refresh token không hợp lệ"
                );
            }

            if (user.getStatus()
                    != UserStatus.ACTIVE) {

                throw new AccountNotActiveException(
                        user.getStatus().name()
                );
            }

            // Rotation: thu hồi refresh token cũ
            session.setRevokedAt(
                    LocalDateTime.now()
            );

            userSessionRepository.save(session);

            CustomUserDetails userDetails =
                    new CustomUserDetails(user);

            String newAccessToken =
                    jwtUtil.generateAccessToken(
                            userDetails
                    );

            String newRefreshToken =
                    jwtUtil.generateRefreshToken(
                            userDetails
                    );

            saveSession(
                    user,
                    newRefreshToken,
                    httpRequest
            );

            return buildAuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    userDetails
            );

        } catch (InvalidTokenException
                 | AccountNotActiveException e) {

            throw e;

        } catch (Exception e) {

            throw new InvalidTokenException(
                    "Refresh token không hợp lệ hoặc đã hết hạn"
            );
        }
    }

    @Override
    public void logout(String refreshToken) {

        if (refreshToken == null
                || refreshToken.isBlank()) {
            return;
        }

        String tokenHash =
                tokenHashUtil.hash(
                        refreshToken
                );

        userSessionRepository
                .findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        tokenHash,
                        LocalDateTime.now()
                )
                .ifPresent(session -> {

                    session.setRevokedAt(
                            LocalDateTime.now()
                    );

                    userSessionRepository.save(session);
                });
    }

    @Override
    public String forgotPassword(
            ForgotPasswordRequest request
    ) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Không tìm thấy tài khoản với email này"
                                )
                        );

        String rawToken =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                + UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        String tokenHash =
                tokenHashUtil.hash(
                        rawToken
                );

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(15)
                        )
                        .build();

        passwordResetTokenRepository.save(
                resetToken
        );

        /*
         * TODO:
         * Sau này không trả rawToken trực tiếp.
         * Gửi token qua Email/Notification service.
         *
         * Hiện tại trả token để dễ test API.
         */

        return rawToken;
    }

    @Override
    public void resetPassword(
            ResetPasswordRequest request
    ) {

        String tokenHash =
                tokenHashUtil.hash(
                        request.getToken()
                );

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Reset token không hợp lệ"
                                )
                        );

        if (resetToken.getUsedAt() != null) {

            throw new InvalidTokenException(
                    "Reset token đã được sử dụng"
            );
        }

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidTokenException(
                    "Reset token đã hết hạn"
            );
        }

        User user =
                resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        resetToken.setUsedAt(
                LocalDateTime.now()
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        // Đăng xuất tất cả session hiện tại
        List<UserSession> sessions =
                userSessionRepository
                        .findByUser_UserIdAndRevokedAtIsNull(
                                user.getUserId()
                        );

        LocalDateTime now =
                LocalDateTime.now();

        sessions.forEach(
                session ->
                        session.setRevokedAt(now)
        );

        userSessionRepository.saveAll(
                sessions
        );
    }

    private void saveSession(
            User user,
            String refreshToken,
            HttpServletRequest request
    ) {

        UserSession session =
                UserSession.builder()
                        .user(user)
                        .refreshTokenHash(
                                tokenHashUtil.hash(
                                        refreshToken
                                )
                        )
                        .userAgent(
                                request.getHeader(
                                        "User-Agent"
                                )
                        )
                        .ipAddress(
                                getClientIp(request)
                        )
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusSeconds(
                                                jwtUtil
                                                        .getRefreshTokenExpirationMs()
                                                        / 1000
                                        )
                        )
                        .build();

        userSessionRepository.save(session);
    }

    private String getClientIp(
            HttpServletRequest request
    ) {

        String forwarded =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwarded != null
                && !forwarded.isBlank()) {

            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private AuthResponse buildAuthResponse(
            String accessToken,
            String refreshToken,
            CustomUserDetails userDetails
    ) {

        User user =
                userRepository
                        .findByEmail(
                                userDetails.getUsername()
                        )
                        .orElseThrow();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(
                        jwtUtil
                                .getAccessTokenExpirationMs()
                                / 1000
                )
                .user(
                        toUserResponse(user)
                )
                .build();
    }

    private UserResponse toUserResponse(
            User user
    ) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roleCode(
                        user.getRole()
                                .getRoleCode()
                )
                .roleName(
                        user.getRole()
                                .getRoleName()
                )
                .status(
                        user.getStatus()
                                .name()
                )
                .createdAt(
                        user.getCreatedAt()
                )
                .build();
    }
}
