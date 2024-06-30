package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.Optional;

import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;

public interface VerificationDomainService {
    void onVerify(Long id,UserApplicationValidation verification, VerificationResponse nextVerifier);

    void onApprove(Long id, UserApplicationValidation verification, VerificationResponse nextApprover);
    void verifyComplete(Long id, Optional<UserApplicationValidation> firstApprover);
    void approveComplete(Long id);

    void sendForReview(Long id,ReferenceObjectDto reviewer, String comment);
}
