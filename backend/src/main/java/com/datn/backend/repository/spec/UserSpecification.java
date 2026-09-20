package com.datn.backend.repository.spec;

import com.datn.backend.entity.Role;
import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Xây dựng điều kiện WHERE động cho danh sách user ở trang quản trị.
// Mỗi tham số có thể null - null nghĩa là "không lọc theo điều kiện đó".
public class UserSpecification {

    private UserSpecification() {
        // utility class, không cho khởi tạo
    }

    public static Specification<User> filter(
            String keyword,
            String roleCode,
            UserStatus status
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {

                String likeKeyword =
                        "%" + keyword.trim().toLowerCase() + "%";

                Predicate fullNameLike = cb.like(
                        cb.lower(root.get("fullName")),
                        likeKeyword
                );

                Predicate emailLike = cb.like(
                        cb.lower(root.get("email")),
                        likeKeyword
                );

                Predicate phoneLike = cb.like(
                        cb.lower(root.get("phone")),
                        likeKeyword
                );

                predicates.add(
                        cb.or(fullNameLike, emailLike, phoneLike)
                );
            }

            if (roleCode != null && !roleCode.isBlank()) {

                jakarta.persistence.criteria.Join<User, Role> roleJoin =
                        root.join("role");

                predicates.add(
                        cb.equal(
                                cb.upper(roleJoin.get("roleCode")),
                                roleCode.toUpperCase()
                        )
                );
            }

            if (status != null) {

                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
