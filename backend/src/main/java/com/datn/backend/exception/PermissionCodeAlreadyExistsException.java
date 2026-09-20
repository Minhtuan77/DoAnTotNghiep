package com.datn.backend.exception;

public class PermissionCodeAlreadyExistsException extends RuntimeException {

    public PermissionCodeAlreadyExistsException(String code) {
        super("permissionCode đã tồn tại: " + code);
    }
}
