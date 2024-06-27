package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.List;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;

public interface VerificationPanelService {
    List<Verifier> getVerifierPanel(Employee employee, Integer fromLevel, Integer toLevel);
}
