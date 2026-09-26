package com.datn.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder
public class OrderStatusHistoryResponse {
    private Long historyId;
    private String fromStatus;
    private String toStatus;
    private Long changedById;
    private String changedByName;
    private String note;
    private LocalDateTime changedAt;
}
