package com.datn.backend.repository.spec;

import com.datn.backend.entity.Product;
import com.datn.backend.entity.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

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

            // Tìm kiếm theo tên hoặc mô tả
            if (keyword != null && !keyword.trim().isEmpty()) {

                String searchPattern =
                        "%" + keyword.trim().toLowerCase() + "%";

                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        searchPattern
                );

                Predicate descLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        searchPattern
                );

                predicates.add(
                        criteriaBuilder.or(nameLike, descLike)
                );
            }

            // Lọc category
            if (categoryId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("categoryId"),
                                categoryId.intValue()
                        )
                );
            }

            // Lọc brand
            if (brandId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("brand").get("brandId"),
                                brandId.intValue()
                        )
                );
            }

            // Giá tối thiểu
            if (minPrice != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"),
                                minPrice
                        )
                );
            }

            // Giá tối đa
            if (maxPrice != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"),
                                maxPrice
                        )
                );
            }

            // Trạng thái
            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}