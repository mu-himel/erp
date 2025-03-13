package com.agi.aesl.erpscm.user_application_validation.service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;

import java.util.Optional;

public interface VerificationDomainService {
    void onVerify(Long id,UserApplicationValidation verification, VerificationResponse nextVerifier);

    void onApprove(Long id, UserApplicationValidation verification, VerificationResponse nextApprover);
    void verifyComplete(Long id, Optional<VerificationResponse> firstApprover);
    void approveComplete(Long id);
    void sendForReview(Long domainId, RefDto reviewer, String comment);

    void onRejected(Employee verifier, Long domainId, RejectDto rejectDto);
}
