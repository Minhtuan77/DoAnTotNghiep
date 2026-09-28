package com.datn.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WishlistResponse {
    private Long wishlistId;
    private Long productId;
    private String productName;
    private String slug;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String primaryImageUrl;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private LocalDateTime addedAt;
}
