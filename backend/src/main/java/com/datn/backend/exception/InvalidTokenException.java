package com.datn.backend.exception;


// Dùng chung cho:
// - refresh token không hợp lệ/hết hạn
// - token reset mật khẩu sai/hết hạn
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}