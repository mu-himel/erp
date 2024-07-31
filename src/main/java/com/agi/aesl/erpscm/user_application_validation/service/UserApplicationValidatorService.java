package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.demand.service.DemandService;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserApplicationValidatorService<T> {
    Optional<VerifierConfig> getVerifiers(ClaimResolver claimResolver, String uri,String criteriaGroup, String categories);
    void setVerifiers(T t, List<Verifier> verifiers, DomainType domainType);
    void setApprovers(T t, List<ApprovalPanel> approvalPanels, DomainType domainType);

    void addVerification(UserApplicationValidation verification);
    void addVerification(List<UserApplicationValidation> verifications);

    List<UserApplicationValidationRepository.VerificationResponse> getVerificationsByDomainTypeAndDomainId(DomainType domainType, Long domainId);

    List<UserApplicationValidation> getVerifyersByDomainId(DomainType cs, Long id);
    void removeVerification(Long id, DomainType demand);
    void setVerificationDomainService(VerificationDomainService verificationDonainService);
    void approve(Jwt token, ApproveDto approveDto);
    void verify(Jwt token, VerifyDto verifyDto);
    void review(VerifyDto verifyDto);

    void reject(RejectDto rejectDto);
}
