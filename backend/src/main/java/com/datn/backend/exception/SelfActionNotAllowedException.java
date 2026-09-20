package com.datn.backend.exception;

// Ngăn Admin tự khóa tài khoản của chính mình hoặc tự đổi role
// của chính mình - tránh trường hợp tự khóa quyền truy cập admin.
public class SelfActionNotAllowedException extends RuntimeException {

    public SelfActionNotAllowedException(String message) {
        super(message);
    }
}
