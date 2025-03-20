package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;

import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class VerificationPanelServiceImpl implements VerificationPanelService{


    private final EntityManager entityManager;

    @Override
    public List<Verifier> getVerifierPanel(Employee employee, Integer fromLevel, Integer toLevel) {
        return new ArrayList<>();
    }
    
}
