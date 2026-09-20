package com.datn.backend.exception;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(Integer permissionId) {
        super("Không tìm thấy permission với ID: " + permissionId);
    }
}
