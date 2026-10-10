package com.datn.backend.repository;

import com.datn.backend.entity.ProductRelation;
import com.datn.backend.entity.enums.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRelationRepository extends JpaRepository<ProductRelation, Long> {
    List<ProductRelation> findTop20ByProduct_ProductIdAndRelationTypeOrderByScoreDesc(Long productId, RelationType relationType);
    Optional<ProductRelation> findByProduct_ProductIdAndRelatedProduct_ProductIdAndRelationType(Long productId, Long relatedProductId, RelationType relationType);
    void deleteByRelationType(RelationType relationType);
}
