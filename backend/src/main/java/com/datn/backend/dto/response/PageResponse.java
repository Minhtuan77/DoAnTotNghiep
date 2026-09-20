package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Wrapper phân trang dùng chung cho MỌI API danh sách trong dự án
// (không chỉ riêng module User) - Product, Order, Voucher... sau này
// đều có thể tái sử dụng lớp này để trả về danh sách có phân trang.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {

    private List<T> content;

    private int page;          // trang hiện tại, bắt đầu từ 0
    private int size;          // số phần tử mỗi trang
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <E, T> PageResponse<T> from(
            Page<E> page,
            Function<E, T> mapper
    ) {

        List<T> content =
                page.getContent()
                        .stream()
                        .map(mapper)
                        .toList();

        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
