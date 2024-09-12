package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem,Long> {

    @Query(value = """
            SELECT * FROM (SELECT i.id, i.brand_id ,i.active,
                    GROUP_CONCAT(DISTINCT  ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit ORDER BY ia.id ASC separator ' - ') itemAttributes
            FROM user_item_attributes ia
            LEFT JOIN user_items i on i.id=ia.item_id
            GROUP BY i.id) p
            WHERE p.brand_id=:brandId AND itemAttributes = :attribute
            """,nativeQuery = true)
    List<?> findByAttributes(Long brandId, String attribute);
}
