package com.datn.backend.repository;

import com.datn.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Notification> findByUser_UserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);
    Optional<Notification> findByNotificationIdAndUser_UserId(Long notificationId, Long userId);
    long countByUser_UserIdAndIsReadFalse(Long userId);
}
