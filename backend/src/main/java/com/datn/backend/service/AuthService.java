package com.datn.backend.service;

import com.datn.backend.dto.request.ForgotPasswordRequest;
import com.datn.backend.dto.request.LoginRequest;
import com.datn.backend.dto.request.RegisterRequest;
import com.datn.backend.dto.request.ResetPasswordRequest;
import com.datn.backend.dto.response.AuthResponse;
import com.datn.backend.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest
    );

    AuthResponse refreshToken(
            String refreshToken,
            HttpServletRequest httpRequest
    );

    void logout(String refreshToken);

    // Trả về token thô để module Notification/Email
    // gửi cho user.
    // Không lưu token thô vào DB, chỉ lưu hash.
    String forgotPassword(
            ForgotPasswordRequest request
    );

    void resetPassword(
            ResetPasswordRequest request
    );
}
