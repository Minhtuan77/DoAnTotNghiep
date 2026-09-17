package com.datn.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// Định dạng lỗi trả về thống nhất cho toàn bộ API
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;

    // HTTP status code, ví dụ 400, 401, 404
    private String error;

    // Ví dụ "BAD_REQUEST"
    private String message;

    // Thông báo chính
    private String path;

    // Endpoint đang gọi
    private List<String> details;

    // Chi tiết lỗi validate nếu có
}