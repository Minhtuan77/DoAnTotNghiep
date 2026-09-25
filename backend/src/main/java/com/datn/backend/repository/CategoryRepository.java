package com.datn.backend.repository;

import com.datn.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
}