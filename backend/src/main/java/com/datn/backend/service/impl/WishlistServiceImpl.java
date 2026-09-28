package com.datn.backend.service.impl;

import com.datn.backend.dto.response.PageResponse;
import com.datn.backend.dto.response.WishlistResponse;
import com.datn.backend.entity.Product;
import com.datn.backend.entity.ProductImage;
import com.datn.backend.entity.User;
import com.datn.backend.entity.Wishlist;
import com.datn.backend.entity.enums.ProductStatus;
import com.datn.backend.exception.BusinessException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.ProductRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.repository.WishlistRepository;
import com.datn.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WishlistResponse add(Long userId, Long productId) {
        if (wishlistRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId)) {
            throw new BusinessException("Sản phẩm đã có trong danh sách yêu thích");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("Sản phẩm hiện không khả dụng");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Wishlist wishlist = wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
        return toResponse(wishlist);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong danh sách yêu thích"));
        wishlistRepository.delete(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WishlistResponse> getMine(Long userId, Pageable pageable) {
        return PageResponse.from(wishlistRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean contains(Long userId, Long productId) {
        return wishlistRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId);
    }

    private WishlistResponse toResponse(Wishlist w) {
        Product p = w.getProduct();
        String image = p.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .findFirst()
                .or(() -> p.getImages().stream().findFirst())
                .map(ProductImage::getImageUrl)
                .orElse(null);
        return WishlistResponse.builder()
                .wishlistId(w.getWishlistId())
                .productId(p.getProductId())
                .productName(p.getName())
                .slug(p.getSlug())
                .price(p.getPrice())
                .salePrice(p.getSalePrice())
                .primaryImageUrl(image)
                .averageRating(p.getAvgRating())
                .reviewCount(p.getReviewCount())
                .addedAt(w.getCreatedAt())
                .build();
    }
}
