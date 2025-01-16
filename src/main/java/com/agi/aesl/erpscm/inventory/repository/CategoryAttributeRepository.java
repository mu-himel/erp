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

    @Query(value= """
            SELECT sca.id,sic.name,sca.attribute_type as attributeType ,
            sca.attribute_value as attributeValue ,sca.attribute_unit as attributeUnit FROM
            scm_category_attributes sca
            LEFT JOIN scm_item_categories sic ON sic.id = sca.category_id
            LEFT JOIN scm_category_warehouse_stores scws ON scws.category_id = sca.category_id
            WHERE sic.code = :code AND scws.warehouse_id = :warehouseId
            AND sca.attribute_type = :attributeType AND sca.attribute_unit = :attributeUnit
            """,nativeQuery = true)
    Optional<ICategoryAttribute> findAllByAttributeTypeAndAttributeUnit(Long warehouseId,String code, String attributeType, String attributeUnit);

    interface ICategoryAttribute{
        Long getId();
        String getName();
        String getAttributeType();
        String getAttributeValue();
        String getAttributeUnit();
    }

    @Modifying
    @Query(value = "DELETE FROM CategoryAttribute ca WHERE ca.category.id = :id")
    void deleteByCategoryId(@Param("id") Long id);
}
