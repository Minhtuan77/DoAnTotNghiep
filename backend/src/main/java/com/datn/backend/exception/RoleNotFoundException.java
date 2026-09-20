package com.datn.backend.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String roleCode) {
        super("Không tìm thấy role: " + roleCode);
    }
}
