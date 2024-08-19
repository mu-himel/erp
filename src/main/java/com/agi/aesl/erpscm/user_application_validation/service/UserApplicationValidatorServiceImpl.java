package com.agi.aesl.erpscm.user_application_validation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.account_finance.repository.UpdateLedgerVerifier;
import com.agi.aesl.erpscm.comment.enums.ActionType;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.comment.entity.Comment;
import com.agi.aesl.erpscm.comment.entity.CommentAttachment;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.ApproveDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.VerifyDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.user_application_validation.dto.response.Verifier;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;
import com.agi.aesl.erpscm.utils.ClaimResolver;

import jakarta.transaction.Transactional;

@Service
public class UserApplicationValidatorServiceImpl<T extends VerifyableEntity> implements UserApplicationValidatorService<T>{

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserApplicationValidationRepository verificationRepository;

    private VerificationDomainService verificationDomainService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ClaimResolver claimResolver;

    @Override
    @Transactional
    public Optional<VerifierConfig> getVerifiers(ClaimResolver claimResolver, String uri, String criteriaGroup, String categories) {
        
        // Map<String,Object> data = new HashMap<>();
        Optional<VerifierConfig> moduleVerifierConfigs = moduleService.getVerifierConfigByModuleAndCriteriaGroup(claimResolver, uri, criteriaGroup, categories);
        
        
        return moduleVerifierConfigs;
    }

    

    @Override
    @Transactional
    public void setApprovers(T t, List<VerifierInfo> verifiers,List<ApprovalPanel> approvalPanels, DomainType domainType,
                             VerifierMailService verifierMailService) {
        if(verifiers.size()==0 && approvalPanels.size()>0){
            Optional<ApprovalPanel> firstPanel = approvalPanels.stream().findFirst();
            if(firstPanel.isPresent()){
                ApprovalPanel panel = firstPanel.get();
                if(verifierMailService!=null) {
                    verifierMailService.prepareMailContent(panel.getName(), "Approval", t);
                    verifierMailService.sentMail(panel.getEmail(),"Pending "+domainType.toString()+" Approval Request");
                }
                t.setStatus("PENDING_APPROVAL");
                t.setNextApproverId(panel.getUserId());
            }
        }

        if(approvalPanels.size()>0){

            List<UserApplicationValidation> verifications = approvalPanels.stream().map(approvalPanel -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(t.getId());
                verification.setDomainType(domainType);
                verification.setVerified(false);
                verification.setIsApproval(true);
                verification.setVerifier(new Employee(approvalPanel.getUserId()));
                return verification;
            }).collect(Collectors.toList());
            addVerification(verifications);
        }
        
    }

  

    @Override
    @Transactional
    public UserApplicationValidatorService<T> setVerifiers(T t, List<VerifierInfo> verifiers,
                                       DomainType domainType, VerifierMailService verifierMailService) {
        if (verifiers.size() > 0) {
            Optional<VerifierInfo> firstOp = verifiers.stream().findFirst();
            VerifierInfo _verifier = firstOp.get();

            if(verifierMailService!=null) {
                verifierMailService.prepareMailContent(_verifier.getName(), "Verification", t);
                verifierMailService.sentMail(_verifier.getEmail(),"Pending "+domainType.toString()+" Verification Request");
            }

            List<UserApplicationValidation> verifications = verifiers.stream().map(verifier -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(t.getId());
                verification.setDomainType(domainType);
                verification.setVerified(false);
                verification.setIsApproval(false);
                verification.setVerifier(new Employee(verifier.getId()));
                return verification;
            }).collect(Collectors.toList());
            t.setNextVerifierId(_verifier.getId());
            addVerification(verifications);
        }
        return this;
    }

    @Override
    @Transactional
    public void addVerification(UserApplicationValidation verification) {
        verificationRepository.save(verification);
        
    }

    @Override
    @Transactional
    public void addVerification(List<UserApplicationValidation> verifications) {
        verificationRepository.saveAll(verifications);
    }

    @Override
    public List<UserApplicationValidation> getVerifyersByDomainId(DomainType domainType, Long id) {
        return verificationRepository.findByDomainTypeAndDomainId(domainType, id);
    }

    @Override
    @Transactional
    public void removeVerification(Long domainId, DomainType domainType) {
        verificationRepository.deleteAllByDomainIdAndDomainType(domainId, domainType);
    }



    @Override
    public List<VerificationResponse> getVerificationsByDomainTypeAndDomainId(DomainType domainType, Long domainId) {
        return verificationRepository.findAllByDomainTypeAndDomainId(domainType,domainId);
    }

    

    @Override
    @Transactional
    public void verify(Jwt token, VerifyDto verifyDto) {
        claimResolver.setToken(token);
        Employee verifier = new Employee(verifyDto.getVerifier().getId());
        DomainType domainType = verifyDto.getDomainType();
        Long domainId = verifyDto.getDomainId();
        String msg = verifyDto.getComment();

        List<UserApplicationValidationRepository.VerificationResponse> count = verificationRepository
                    .findAllByDomainTypeAndDomainIdAndVerifiedAndIsApproval(
                            domainType, domainId,false,false);


        Optional<UserApplicationValidation> verificationOp = verificationRepository
                .findByDomainTypeAndDomainIdAndVerifierAndIsApproval(domainType,domainId,verifier,false);
        if(verificationOp.isPresent()){
            UserApplicationValidation verification = verificationOp.get();
            
            verification.setVerified(true);
            verification.setVerificationDate(LocalDateTime.now());
            verificationRepository.save(verification);
            if(count!=null && count.size()>1 && verificationDomainService!=null){
                if(count.get(1)!=null) {
                    verificationDomainService.onVerify(domainId, verification, count.get(1));
                }
            }

            if(count!=null && count.size()==1 && verificationDomainService!=null){
                List<VerificationResponse> approvalCount = verificationRepository
                        .findAllByDomainTypeAndDomainIdAndVerifiedAndIsApproval(
                                domainType, domainId,false,true);
                Optional<VerificationResponse> firstApprover = Optional.empty();
                if(approvalCount.size()>0){
                    firstApprover = approvalCount.stream().findFirst();
                }
                verificationDomainService.verifyComplete(domainId,firstApprover);
            }

            comment(verifier, domainType, ActionType.VERIFICATION, domainId, msg,verifyDto.getAttachments());
        }
        
    }



    @Override
    @Transactional
    public void approve(Jwt token, ApproveDto verifyDto) {
        claimResolver.setToken(token);
        Employee verifier = new Employee(verifyDto.getVerifier().getId());
        DomainType domainType = verifyDto.getDomainType();
        Long domainId = verifyDto.getDomainId();
        String msg = verifyDto.getComment();

        List<UserApplicationValidationRepository.VerificationResponse> count = verificationRepository
                .findAllByDomainTypeAndDomainIdAndVerifiedAndIsApproval(
                        domainType, domainId,false,true);

        Optional<UserApplicationValidation> verificationOp = verificationRepository
                .findByDomainTypeAndDomainIdAndVerifierAndIsApproval(domainType,domainId,verifier,true);

        if(verificationOp.isPresent()) {
            UserApplicationValidation verification = verificationOp.get();
            verification.setVerified(true);
            verification.setVerificationDate(LocalDateTime.now());

            if(count!=null && count.size()>1 && verificationDomainService!=null){
                if(count.get(1)!=null) {
                    verificationDomainService.onApprove(domainId,verification, count.get(1));
                }
            }
            if(count!=null && count.size()==1 && verificationDomainService!=null){
                verificationDomainService.approveComplete(domainId);
            }

            comment(verifier, domainType, ActionType.APPROVAL, domainId, msg, verifyDto.getAttachments());
        }
        
    }

    

    @Override
    @Transactional
    public void review(VerifyDto verifyDto) {
        if(verificationDomainService!=null){
            if((verifyDto.getComment()==null || verifyDto.getComment().isEmpty())){
                throw new RuntimeException("Message Required");
            }
            verificationDomainService.sendForReview(verifyDto.getDomainId(),verifyDto.getReviewer(),verifyDto.getComment());

            comment(new Employee(verifyDto.getVerifier().getId()),
                    verifyDto.getDomainType(), verifyDto.getActionType(), verifyDto.getDomainId(),
                    verifyDto.getComment(),verifyDto.getAttachments());
        }
        
    }

    private void comment(Employee verifier, DomainType domainType, ActionType actionType, Long domainId, String msg, List<CommentAttachment> attachments) {
        if(msg !=null && !msg.isEmpty()){
            Comment comment = commentService.prepareComment(verifier,domainType, actionType,domainId,msg, attachments);
            commentService.addComment(comment);
        }
    }

    private void comment(Employee verifier, DomainType domainType, Long domainId, String msg, List<CommentAttachment> attachments) {
        if(msg !=null && !msg.isEmpty()){
            Comment comment = commentService.prepareComment(verifier,domainType,domainId,msg, attachments);
            commentService.addComment(comment);
        }
    }

    @Override
    @Transactional
    public void reject(Jwt token, RejectDto rejectDto) {
        claimResolver.setToken(token);
        if(verificationDomainService!=null){
            if((rejectDto.getComment()==null || rejectDto.getComment().isEmpty())){
                throw new RuntimeException("Message Required");
            }

            verificationDomainService.onRejected(claimResolver.getEmployee().get(),rejectDto.getDomainId());

            comment(new Employee(rejectDto.getVerifier().getId()),
                    rejectDto.getDomainType(),
                    rejectDto.getActionType(),rejectDto.getDomainId(),
                    rejectDto.getComment(),rejectDto.getAttachments());
        }
    }

    public void setVerificationDomainService(VerificationDomainService verificationDomainService){
        this.verificationDomainService = verificationDomainService;
    }


    @Override
    public Optional<UserApplicationValidation> getVerificationsByDomainTypeAndDomainIdAndVerifierId(DomainType accountLedger, Long domainId, Employee verifier) {
       return verificationRepository.findByDomainTypeAndDomainIdAndVerifierAndIsApproval(
            DomainType.ACCOUNT_LEDGER,domainId,verifier,false
       );
    }

    @Override
    @Transactional
    public <T extends VerifyableEntity> List<VerifierInfo> getVerifiers(T ledgerAccount, Optional<VerifierConfig> verifierOp, String status) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            verifiers = verification.getVerifiers();
            Boolean verificationRequired = verification.getVerificationRequired();
            if(verificationRequired!=null && verificationRequired==true && verifiers!=null && verifiers.size()>0){
                ledgerAccount.setStatus(AccountType.PENDING_VERIFICATION.toString());
            }else{
                ledgerAccount.setStatus(status);
            }

        }else{
            ledgerAccount.setStatus(status);
        }
        return verifiers;
    }

    @Override
    @Transactional
    public List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver, String uri, String categories) {
        List<ApprovalPanel> approvalPanels = moduleService.getModuleWiseApprovalSetting(claimResolver,uri,
                Optional.ofNullable(categories),Optional.empty());
        return approvalPanels;
    }

    @Override
    public AppliedVADto applyVerifyApprovalProcess(T t, DomainType domainType, String uri, String criteriaGroup, List<String> ids) {
        Optional<VerifierConfig> verifierOp = this.getVerifiers(claimResolver, uri,
                criteriaGroup, String.join(",", ids));

        List<VerifierInfo> verifiers = this.getVerifiers(t, verifierOp,domainType.toString());
        List<ApprovalPanel> panels = this.getApprovalPanels(claimResolver, uri, String.join(",", ids));
        this.setVerifiers(t, verifiers, domainType,
                        null)
                .setApprovers(t,verifiers,panels,domainType,null);

        return new AppliedVADto(verifiers,panels);
    }
}
