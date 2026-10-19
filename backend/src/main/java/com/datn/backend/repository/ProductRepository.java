package com.datn.backend.repository;

import com.datn.backend.entity.Product;
import com.datn.backend.entity.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByProductIdAndStatus(Long productId, ProductStatus status);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    boolean existsByCategory_CategoryId(Integer categoryId);

    boolean existsByBrand_BrandId(Integer brandId);

    boolean existsByName(String name);

    boolean existsBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    long countByStatus(ProductStatus status);

    /**
     * Tăng lượt xem theo một câu UPDATE duy nhất để tránh read-modify-save và
     * hạn chế lost update khi nhiều người mở cùng một sản phẩm đồng thời.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.viewCount = coalesce(p.viewCount, 0) + 1 where p.productId = :productId")
    int incrementViewCount(@Param("productId") Long productId);
}