package com.agi.aesl.erpscm.user_application_validation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Optional<VerifierConfig> getVerifiers(ClaimResolver claimResolver, String uri, String criteriaGroup, String categories) {
        
        // Map<String,Object> data = new HashMap<>();
        Optional<VerifierConfig> moduleVerifierConfigs = moduleService.getVerifierConfigByModuleAndCriteriaGroup(claimResolver, uri, criteriaGroup, categories);
        
        
        return moduleVerifierConfigs;
    }

    

    @Override
    public void setApprovers(T t, List<ApprovalPanel> approvalPanels, DomainType domainType) {
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
    public void setVerifiers(T t, List<Verifier> verifiers, DomainType domainType) {
        if (verifiers.size() > 0) {
            Optional<Verifier> firstOp = verifiers.stream().findFirst();
            Verifier _verifier = firstOp.get();
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
    }

    @Override
    public void addVerification(UserApplicationValidation verification) {
        verificationRepository.save(verification);
        
    }

    @Override
    public void addVerification(List<UserApplicationValidation> verifications) {
        verificationRepository.saveAll(verifications);
    }

    @Override
    public List<UserApplicationValidation> getVerifyersByDomainId(DomainType domainType, Long id) {
        return verificationRepository.findByDomainTypeAndDomainId(domainType, id);
    }

    @Override
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

            comment(verifier, domainType, domainId, msg,verifyDto.getAttachments());
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

            comment(verifier, domainType, domainId, msg, verifyDto.getAttachments());
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
                    verifyDto.getDomainType(),verifyDto.getDomainId(),
                    verifyDto.getComment(),verifyDto.getAttachments());
        }
        
    }



    private void comment(Employee verifier, DomainType domainType, Long domainId, String msg, List<CommentAttachment> attachments) {
        if(msg !=null && !msg.isEmpty()){
            Comment comment = commentService.prepareComment(verifier,domainType,domainId,msg, attachments);
            commentService.addComment(comment);
        }
    }



    public void setVerificationDomainService(VerificationDomainService verificationDomainService){
        this.verificationDomainService = verificationDomainService;
    }


    
    
    

    
    
}
