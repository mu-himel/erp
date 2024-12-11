package com.agi.aesl.erpscm.inventory.user_request.repository;

import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCategoryAttributeRepository extends JpaRepository<UserCategoryAttribute,Long> {
    Optional<UserCategoryAttribute> findAllByAttributeTypeAndAttributeUnit(String attributeType, String attributeUnit);
}
