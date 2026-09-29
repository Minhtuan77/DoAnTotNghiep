package com.datn.backend.service;

import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    PageResponse<NotificationResponse> getMine(Long userId, Boolean isRead, Pageable pageable);
    long countUnread(Long userId);
    NotificationResponse markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void deleteMine(Long userId, Long notificationId);

    void create(User user, String title, String content, NotificationType type,
                String referenceType, Long referenceId);
}
