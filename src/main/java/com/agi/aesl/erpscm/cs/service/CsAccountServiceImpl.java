package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.cs.entity.CsAccountVAHistory;
import com.agi.aesl.erpscm.cs.entity.CsVerificationApprovalHistory;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.cs.repository.CsAccountRepository;
import com.agi.aesl.erpscm.cs.repository.CsAccountVAHistoryRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CsAccountServiceImpl implements CsAccountService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private ClaimResolver claimResolver;
    @Autowired
    private CsAccountRepository csAccountRepository;

    @Autowired
    private CsAccountVAHistoryRepository csAccountVAHistoryRepo;

    @Autowired
    private UserApplicationValidatorService<CsAccount> verificationService;

    @Autowired
    private CommentService commentService;

    @Override
    @Transactional
    public void createCsAccount(Cs cs) {
        CsAccount csAccount = new CsAccount();
        csAccount.setCs(cs);
        csAccount.setAcsStatus(CsStatus.PENDING);
        csAccountRepository.save(csAccount);
    }

    @Override
    @Transactional
    public void updateCsAccount(Long id, Jwt token, String uri, AcsUpdateDto acsUpdateDto) {
        claimResolver.setToken(token);
        Optional<CsAccount> csAccountOp = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            List<String> ids= new ArrayList<>();
            csAccount.getCs().getCsDetails().forEach(csd->{
                ids.add(csd.getIndentDetail().getSubCategory().getId().toString());
                ids.add(csd.getIndentDetail().getSubCategory().getParentCategory().getId().toString());
            });
            AppliedVADto vaDto = verificationService.applyVerifyApprovalProcess(csAccount, DomainType.ACS, CsStatus.COMPLETED.toString(),
                    uri, "CATEGORY", ids, null);

            if(vaDto.getVerifiers().isEmpty() && vaDto.getPanels().isEmpty()){
                csAccount.setAcsStatus(CsStatus.COMPLETED);
            }
        }
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<CsAccount> csAccOp = csAccountRepository.findById(id);
        if(csAccOp.isPresent()){
            CsAccount cs = csAccOp.get();
            cs.setNextVerifierId(nextVerifier.getVerifier().getId());
            cs.setAcsStatus(CsStatus.VERIFIED);
            setVAHistory(cs, CsStatus.VERIFIED);
        }
    }

    @Transactional
    private void setVAHistory(CsAccount csAccount, CsStatus status){
        CsAccountVAHistory csVaHistory = new CsAccountVAHistory();
        String empId = null;
        if (status.equals(CsStatus.VERIFIED)){
            empId =csAccount.getNextVerifierId();
        }else if(status.equals(CsStatus.APPROVED)){
            empId = csAccount.getNextApproverId();
        }
        csVaHistory.setCsAccount(csAccount);
        csVaHistory.setEmployee(new Employee(empId));
        csVaHistory.setAcsStatus(status);
        csAccountVAHistoryRepo.save(csVaHistory);
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setNextApproverId(nextApprover.getVerifier().getId());
            csAccount.setAcsStatus(CsStatus.APPROVED);
            setVAHistory(csAccount, CsStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            if(firstApprover.isPresent()){
                csAccount.setNextApproverId(firstApprover.get().getVerifier().getId());
                csAccount.setAcsStatus(CsStatus.PENDING_APPROVAL);
            }else {
                csAccount.setAcsStatus(CsStatus.VERIFIED);
            }
            setVAHistory(csAccount,CsStatus.VERIFIED);

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setAcsStatus(CsStatus.APPROVED);
            setVAHistory(csAccount,CsStatus.APPROVED);

        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(domainId);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setReviewerId(reviewer.getId());
            csAccount.setReviewPrevStatus(csAccount.getAcsStatus());
            csAccount.setAcsStatus(CsStatus.REVIEW);

        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }
    
    

    @Override
    public Page<?> getPendingAcs(Jwt token,
                                 Optional<String> indentNo, Optional<String> status,
                                 Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> _status = new ArrayList<>();
        status.ifPresent(_status::add);
        return csAccountRepository.findAllAcs(indentNo, _status,pageable);
    }

    private static Pageable getPageable(Optional<Integer> page, Optional<Integer> size) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Integer _size = (size.get().equals(-1))? Integer.MAX_VALUE: size.orElse(PAGE_SIZE);
        Pageable pageable = PageRequest.of(page.orElse(0),_size);
        return pageable;
    }

    @Override
    public Page<?> getPendingVerificationAcs(Jwt token, Optional<String> indentNo, Optional<Integer> page,
                                             Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> _status = new ArrayList<>();
        _status.add(CsStatus.PENDING_VERIFICATION.toString());
        _status.add(CsStatus.REVIEW.toString());
        _status.add(CsStatus.VERIFIED.toString());
        return csAccountRepository.findAllPendingVerificationAcs(indentNo, _status,pageable);
    }

    @Override
    public Page<?> getApprovedAcs(Jwt token,
                                  Optional<String> indentNo,
                                  Optional<Integer> page,
                                  Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> _status = new ArrayList<>();
        _status.add(CsStatus.APPROVED.toString());
        _status.add(CsStatus.VERIFIED.toString());
        return csAccountRepository.findAllAcs(indentNo, _status,pageable);
    }

    @Override
    public Page<?> getRejectedAcs(Jwt token, Optional<String> indentNo, Optional<Integer> page,
                                  Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> _status = new ArrayList<>();
        _status.add(CsStatus.REJECTED.toString());
        return csAccountRepository.findAllAcs(indentNo, _status,pageable);
    }

    @Override
    public Page<?> getClosedAcs(Jwt token, Optional<String> indentNo, Optional<String> status, Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> _status = new ArrayList<>();
        if(status.isEmpty()) {
            _status.add(CsStatus.APPROVED.toString());
            _status.add(CsStatus.REJECTED.toString());
            _status.add(CsStatus.COMPLETED.toString());
        }
        status.ifPresent(_status::add);
        return csAccountRepository.findAllAcs(indentNo, _status,pageable);
    }

    @Override
    @Transactional
    public void reviewAcs(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<CsAccount> csAccountOp = csAccountRepository.findById(id);
        if(csAccountOp.isEmpty()){
            throw new RuntimeException("Sorry! Account Cs  not found");
        }

        CsAccount csAccount = csAccountOp.get();
        csAccount.setReviewerId(null);
        csAccount.setReviewDate(LocalDateTime.now());
        csAccount.setAcsStatus(csAccount.getReviewPrevStatus());
        csAccount.setReviewPrevStatus(null);

        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),
                        DomainType.ACS,csAccount.getId(),noteDto.getNote(),noteDto.getAttachments())
        );
    }
}
