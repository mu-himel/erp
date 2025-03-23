package com.agi.aesl.erpscm.user_application_validation.service;

import java.time.LocalDateTime;
import java.util.*;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.ActionType;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository.VerificationResponse;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserApplicationValidatorServiceImpl<T extends VerifyableEntity> implements UserApplicationValidatorService<T>{


    private final ModuleService moduleService;


    private final UserApplicationValidationRepository verificationRepository;

    @Setter
    private VerificationDomainService verificationDomainService;

    private Map<String, VerificationDomainService> verificationDomainServiceMap=new HashMap<>();


    private final CommentService commentService;


    private final ClaimResolver claimResolver;

    @Override
    @Transactional
    public Optional<VerifierConfig> prepareLogicForVerifiers(ClaimResolver claimResolver, String uri, String criteriaGroup, String categories) {

        return moduleService.getVerifierConfigByModuleAndCriteriaGroup(claimResolver, uri, criteriaGroup, categories);

    }

    

    @Override
    @Transactional
    public void setApprovers(T t, List<VerifierInfo> verifiers,List<ApprovalPanel> approvalPanels, DomainType domainType,
                             VerifierMailService<T> verifierMailService) {
        if(verifiers.isEmpty() && !approvalPanels.isEmpty()){
            Optional<ApprovalPanel> firstPanel = approvalPanels.stream().findFirst();
            firstPanel.ifPresent( fp->{
                if(verifierMailService!=null) {
                    verifierMailService.prepareMailContent(fp.getName(), "Approval", t);
                    verifierMailService.sentMail(fp.getEmail(),"Pending "+domainType.toString()+" Approval Request");
                }
                t.setStatus("PENDING_APPROVAL");
                t.setNextApproverId(fp.getUserId());
                }
            );
        }

        if(!approvalPanels.isEmpty()){

            List<UserApplicationValidation> verifications = approvalPanels.stream().map(approvalPanel -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(t.getId());
                verification.setDomainType(domainType);
                verification.setVerified(false);
                verification.setIsApproval(true);
                verification.setVerifier(new Employee(approvalPanel.getUserId()));
                return verification;
            }).toList();
            addVerification(verifications);
        }
        
    }

  

    @Override
    @Transactional
    public UserApplicationValidatorService<T> setVerifiers(T t, List<VerifierInfo> verifiers,
                                       DomainType domainType, VerifierMailService<T> verifierMailService) {
        if (!verifiers.isEmpty()) {
            Optional<VerifierInfo> firstOp = verifiers.stream().findFirst();
            VerifierInfo verifier = firstOp.get();

            if(verifierMailService!=null) {
                verifierMailService.prepareMailContent(verifier.getName(), "Verification", t);
                verifierMailService.sentMail(verifier.getEmail(),"Pending "+domainType.toString()+" Verification Request");
            }

            List<UserApplicationValidation> verifications = verifiers.stream().map(vf -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(t.getId());
                verification.setDomainType(domainType);
                verification.setVerified(false);
                verification.setIsApproval(false);
                verification.setVerifier(new Employee(vf.getId()));
                return verification;
            }).toList();
            t.setNextVerifierId(verifier.getId());
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
        verificationDomainService = this.verificationDomainServiceMap.get(verifyDto.getDomainType().name());
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
            if(count!=null && count.size()>1 && verificationDomainService!=null && count.get(1)!=null) {
                verificationDomainService.onVerify(domainId, verification, count.get(1));
            }


            if(count!=null && count.size()==1 && verificationDomainService!=null){
                List<VerificationResponse> approvalCount = verificationRepository
                        .findAllByDomainTypeAndDomainIdAndVerifiedAndIsApproval(
                                domainType, domainId,false,true);
                Optional<VerificationResponse> firstApprover = Optional.empty();
                if(!approvalCount.isEmpty()){
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
        verificationDomainService = this.verificationDomainServiceMap.get(verifyDto.getDomainType().name());
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

            if(count!=null && count.size()>1 && verificationDomainService!=null && count.get(1)!=null) {
                    verificationDomainService.onApprove(domainId,verification, count.get(1));
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
        verificationDomainService = this.verificationDomainServiceMap.get(verifyDto.getDomainType().name());
        if(verificationDomainService!=null){
            if((verifyDto.getComment()==null || verifyDto.getComment().isEmpty())){
                throw new AesException("Message Required");
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



    @Override
    @Transactional
    public void reject(Jwt token, RejectDto rejectDto) {
        claimResolver.setToken(token);
        verificationDomainService = this.verificationDomainServiceMap.get(rejectDto.getDomainType().name());
        if(verificationDomainService!=null){
            if((rejectDto.getComment()==null || rejectDto.getComment().isEmpty())){
                throw new AesException("Message Required");
            }

            verificationDomainService.onRejected(claimResolver.getEmployee().get(),rejectDto.getDomainId(), rejectDto);

            comment(new Employee(rejectDto.getVerifier().getId()),
                    rejectDto.getDomainType(),
                    rejectDto.getActionType(),rejectDto.getDomainId(),
                    rejectDto.getComment(),rejectDto.getAttachments());
        }
    }


    @Override
    public void addVerificationDomainService(String  domainType, VerificationDomainService verificationDomainService) {
        if(!this.verificationDomainServiceMap.containsKey(domainType)){
            this.verificationDomainServiceMap.put(domainType,verificationDomainService);
        }
    }

    @Override
    public Optional<UserApplicationValidation> getVerificationsByDomainTypeAndDomainIdAndVerifierId(DomainType accountLedger, Long domainId, Employee verifier) {
       return verificationRepository.findByDomainTypeAndDomainIdAndVerifierAndIsApproval(
            DomainType.ACCOUNT_LEDGER,domainId,verifier,false
       );
    }

    @Override
    @Transactional
    public <T extends VerifyableEntity> List<VerifierInfo> prepareLogicForVerifiers(T t, Optional<VerifierConfig> verifierOp, String status) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            if(!verification.getVerifiers().isEmpty()) {
                verifiers = verification.getVerifiers();
            }
            Boolean verificationRequired = verification.getVerificationRequired();
            if(verificationRequired != null && verificationRequired && !verifiers.isEmpty()){
                t.setStatus(AccountType.PENDING_VERIFICATION.toString());
            }else{
                t.setStatus(status);
            }

        }else{
            t.setStatus(status);
        }
        return verifiers;
    }

    @Override
    @Transactional
    public List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver, String uri, String categories) {
        return moduleService.getModuleWiseApprovalSetting(claimResolver,uri,
                Optional.ofNullable(categories),Optional.empty());
    }

    @Override
    @Transactional
    public AppliedVADto applyVerifyApprovalProcess(T t, DomainType domainType, String status, String uri, String criteriaGroup,
                                                   List<String> ids,
                                                   VerifierMailService<T> mailService) {
        Optional<VerifierConfig> verifierOp = this.prepareLogicForVerifiers(claimResolver, uri,
                criteriaGroup, String.join(",", ids));

        List<VerifierInfo> verifiers = this.prepareLogicForVerifiers(t, verifierOp,status);
        List<ApprovalPanel> panels = this.getApprovalPanels(claimResolver, uri, String.join(",", ids));
        this.setVerifiers(t, verifiers, domainType,
                        mailService)
                .setApprovers(t,verifiers,panels,domainType,mailService);

        return new AppliedVADto(verifiers,panels);
    }
}
