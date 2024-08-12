package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute,Long> {
    List<CategoryAttribute> findAllByCategoryId(Long categoryId);

    void deleteByIdAndCategoryId(Long attributeId, Long categoryId);

    Optional<CategoryAttribute> findAllByAttributeTypeAndAttributeUnit(String attributeType, String attributeUnit);

    @Modifying
    @Query(value = "DELETE FROM CategoryAttribute ca WHERE ca.category.id = :id")
    void deleteByCategoryId(@Param("id") Long id);
}
