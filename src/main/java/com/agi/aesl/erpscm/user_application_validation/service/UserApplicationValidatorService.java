package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserApplicationValidatorService<T> {
    Optional<VerifierConfig> prepareLogicForVerifiers(ClaimResolver claimResolver, String uri, String criteriaGroup, String categories);
    UserApplicationValidatorService<T> setVerifiers(T t, List<VerifierInfo> verifiers, DomainType domainType,
                                                    VerifierMailService<T> verifierMailService);
    void setApprovers(T t, List<VerifierInfo> verifiers,List<ApprovalPanel> approvalPanels, DomainType domainType,
                      VerifierMailService<T> verifierMailService);

    void addVerification(UserApplicationValidation verification);
    void addVerification(List<UserApplicationValidation> verifications);

    List<UserApplicationValidationRepository.VerificationResponse> getVerificationsByDomainTypeAndDomainId(DomainType domainType, Long domainId);

    List<UserApplicationValidation> getVerifyersByDomainId(DomainType cs, Long id);
    void removeVerification(Long id, DomainType demand);
    void setVerificationDomainService(VerificationDomainService verificationDomainService);
    void addVerificationDomainService(String domainType, VerificationDomainService verificationDomainService);
    void approve(Jwt token, ApproveDto approveDto);
    void verify(Jwt token, VerifyDto verifyDto);
    void review(VerifyDto verifyDto);

    void reject(Jwt token, RejectDto rejectDto);

    Optional<UserApplicationValidation> getVerificationsByDomainTypeAndDomainIdAndVerifierId(
            DomainType accountLedger,
            Long id,
            Employee verifier);

    <T extends VerifyableEntity> List<VerifierInfo> prepareLogicForVerifiers(T ledgerAccount, Optional<VerifierConfig> verifierOp, String status);

    List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver,String uri, String categories);

    AppliedVADto applyVerifyApprovalProcess(T t, DomainType domainType, String status, String uri, String criteriaGroup, List<String> ids,
                                            VerifierMailService<T> mailService);

}
