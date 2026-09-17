package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;

    private String fullName;

    private String email;

    private String phone;

    private String avatarUrl;

    private String roleCode;

    private String roleName;

    private String status;

    private LocalDateTime createdAt;
}