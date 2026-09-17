package com.datn.backend.exception;


public class PhoneAlreadyExistsException extends RuntimeException {

    public PhoneAlreadyExistsException(String phone) {
        super("Số điện thoại đã được sử dụng: " + phone);
    }
}
