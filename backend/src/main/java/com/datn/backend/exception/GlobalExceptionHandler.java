package com.datn.backend.exception;


import com.datn.backend.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            PhoneAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler({
            InvalidCredentialsException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            RuntimeException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotActive(
            AccountNotActiveException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler({
            RoleNotFoundException.class,
            PermissionNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleAdminNotFound(
            RuntimeException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(PermissionCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionCodeConflict(
            PermissionCodeAlreadyExistsException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler({
            InvalidRoleAssignmentException.class,
            SelfActionNotAllowedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleAdminBadRequest(
            RuntimeException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                req,
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền thực hiện thao tác này",
                req,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        return build(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu gửi lên không hợp lệ",
                req,
                details
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest req
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã có lỗi xảy ra ở hệ thống, vui lòng thử lại sau",
                req,
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest req,
            List<String> details
    ) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity
                .status(status)
                .body(body);
    }
}