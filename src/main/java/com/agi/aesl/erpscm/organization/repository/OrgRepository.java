package com.agi.aesl.erpscm.organization.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.organization.entity.Organization;

@Repository
public interface OrgRepository extends JpaRepository<Organization,Long>{

    Optional<Organization> findByOrgCode(String code);
    
}
