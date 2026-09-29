package com.datn.backend.service.impl;

import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.entity.Notification;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.NotificationType;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.NotificationRepository;
import com.datn.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public PageResponse<NotificationResponse> getMine(Long userId, Boolean isRead, Pageable pageable) {
        Page<Notification> page = isRead == null
                ? notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = getOwned(userId, notificationId);
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification = notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        // Dùng page lớn theo từng lô để tránh load toàn bộ lịch sử thông báo vào RAM.
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 200);
        Page<Notification> page;
        do {
            page = notificationRepository.findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
            if (page.isEmpty()) break;
            page.getContent().forEach(n -> n.setIsRead(true));
            notificationRepository.saveAll(page.getContent());
            // Sau khi đổi sang read, trang 0 sẽ chứa lô unread tiếp theo.
        } while (page.hasContent());
    }

    @Override
    @Transactional
    public void deleteMine(Long userId, Long notificationId) {
        notificationRepository.delete(getOwned(userId, notificationId));
    }

    @Override
    @Transactional
    public void create(User user, String title, String content, NotificationType type,
                       String referenceType, Long referenceId) {
        if (user == null) return;
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type == null ? NotificationType.SYSTEM : type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build());
    }

    private Notification getOwned(Long userId, Long notificationId) {
        return notificationRepository.findByNotificationIdAndUser_UserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
