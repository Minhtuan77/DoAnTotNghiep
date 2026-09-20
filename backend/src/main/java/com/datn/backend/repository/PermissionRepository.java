package com.datn.backend.repository;

import com.datn.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    boolean existsByPermissionCode(String permissionCode);
}
