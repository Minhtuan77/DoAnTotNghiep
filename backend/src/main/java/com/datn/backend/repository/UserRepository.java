package com.datn.backend.repository;

import com.datn.backend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// JpaSpecificationExecutor cho phép AdminUserServiceImpl build query
// động (lọc theo keyword/roleCode/status cùng lúc, có thể thiếu bất kỳ
// điều kiện nào) mà không cần viết nhiều @Query thủ công - xem
// repository/spec/UserSpecification.java
public interface UserRepository
        extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {
            "role",
            "role.permissions"
    })
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}