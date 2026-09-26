package com.datn.backend.repository;

import com.datn.backend.entity.Product;
import com.datn.backend.entity.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}