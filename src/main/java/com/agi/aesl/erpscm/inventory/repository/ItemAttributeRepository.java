package com.agi.aesl.erpscm.inventory.repository;

import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemAttributeRepository extends JpaRepository<ItemAttribute,Long> {
    Optional<ItemAttribute> findAllByAttributeTypeAndAttributeUnitAndItemId(String attributeType, String attributeUnit, Long id);
}
