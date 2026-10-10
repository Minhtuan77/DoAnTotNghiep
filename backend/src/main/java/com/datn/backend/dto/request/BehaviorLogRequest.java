package com.datn.backend.dto.request;

import com.datn.backend.entity.enums.BehaviorActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BehaviorLogRequest {
    private Long productId;
    @NotNull
    private BehaviorActionType actionType;
    private String searchKeyword;
    private String sessionId;
    private String metadata;
}
