package com.agi.aesl.erpscm.user_application_validation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationPanelService;

@Repository
public interface UserApplicationValidationRepository extends JpaRepository<UserApplicationValidation,Long>,VerificationPanelService{
    
}
