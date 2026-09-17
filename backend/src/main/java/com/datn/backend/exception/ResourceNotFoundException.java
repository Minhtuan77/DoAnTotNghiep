package com.datn.backend.exception;


// Dùng chung cho mọi trường hợp không tìm thấy bản ghi
// như user, address...
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}