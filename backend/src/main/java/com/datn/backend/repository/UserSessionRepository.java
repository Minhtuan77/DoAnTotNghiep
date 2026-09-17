package com.datn.backend.repository;

import com.datn.backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // Chỉ lấy session còn hiệu lực:
    // chưa bị thu hồi (revokedAt = NULL) và chưa hết hạn
    Optional<UserSession> findByRefreshTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String refreshTokenHash,
            LocalDateTime now
    );

    // Lấy tất cả session chưa bị thu hồi của user
    List<UserSession> findByUser_UserIdAndRevokedAtIsNull(
            Long userId
    );
}