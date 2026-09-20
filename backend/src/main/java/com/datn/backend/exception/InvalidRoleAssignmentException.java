package com.datn.backend.exception;

// Ném ra khi Admin cố gán một roleCode không hợp lệ cho thao tác
// đang thực hiện, ví dụ: tạo tài khoản Staff nhưng truyền roleCode = "CUSTOMER"
// (CUSTOMER phải đi qua /api/auth/register), hoặc roleCode không tồn tại.
public class InvalidRoleAssignmentException extends RuntimeException {

    public InvalidRoleAssignmentException(String message) {
        super(message);
    }
}
