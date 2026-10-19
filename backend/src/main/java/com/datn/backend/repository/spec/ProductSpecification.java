package com.datn.backend.repository.spec;

import com.datn.backend.entity.Product;
import com.datn.backend.entity.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> filterProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            /*
             * MySQL's normal utf8mb4 *_ci collations are case-insensitive, so using
             * LIKE directly is sufficient for the product search. More importantly,
             * avoiding lower()/upper() here prevents Hibernate 6 function-argument
             * validation problems when an existing DB column has a text/LOB JDBC type.
             * This is especially relevant for projects whose schema has evolved via
             * ddl-auto=update.
             */
            if (keyword != null && !keyword.isBlank()) {
                String searchPattern = "%" + escapeLike(keyword.trim()) + "%";

                Predicate nameLike = criteriaBuilder.like(
                        root.get("name").as(String.class),
                        searchPattern,
                        '\\'
                );

                Predicate skuLike = criteriaBuilder.like(
                        root.get("sku").as(String.class),
                        searchPattern,
                        '\\'
                );

                Predicate shortDescLike = criteriaBuilder.like(
                        root.get("shortDesc").as(String.class),
                        searchPattern,
                        '\\'
                );

                predicates.add(criteriaBuilder.or(nameLike, skuLike, shortDescLike));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("categoryId"),
                        categoryId.intValue()
                ));
            }

            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("brand").get("brandId"),
                        brandId.intValue()
                ));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
