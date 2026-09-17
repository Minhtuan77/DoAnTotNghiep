package com.datn.backend.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String status) {
        super("Tài khoản đang ở trạng thái '" + status + "', không thể đăng nhập");
    }
}