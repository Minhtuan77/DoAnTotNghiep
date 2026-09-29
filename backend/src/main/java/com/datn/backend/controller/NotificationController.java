package com.datn.backend.controller;

import com.datn.backend.dto.response.ApiResponse;
import com.datn.backend.dto.response.NotificationResponse;
import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Boolean isRead,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công",
                notificationService.getMine(user.getUserId(), isRead, pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Lấy số thông báo chưa đọc thành công",
                Map.of("unreadCount", notificationService.countUnread(user.getUserId()))));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu thông báo là đã đọc",
                notificationService.markAsRead(user.getUserId(), notificationId)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails user) {
        notificationService.markAllAsRead(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả thông báo là đã đọc", null));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteMine(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long notificationId) {
        notificationService.deleteMine(user.getUserId(), notificationId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa thông báo", null));
    }
}
