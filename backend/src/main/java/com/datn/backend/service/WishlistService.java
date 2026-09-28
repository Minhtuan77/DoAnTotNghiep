package com.datn.backend.service;

import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.WishlistResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistResponse add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    PageResponse<WishlistResponse> getMine(Long userId, Pageable pageable);
    boolean contains(Long userId, Long productId);
}
