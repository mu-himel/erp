package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;

import jakarta.persistence.EntityManager;

@Service
public class VerificationPanelServiceImpl implements VerificationPanelService{

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Verifier> getVerifierPanel(Employee employee, Integer fromLevel, Integer toLevel) {
        return null;
    }
    
}
