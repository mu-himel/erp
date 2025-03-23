package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.cs.entity.CsAccountVAHistory;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.cs.repository.CsAccountRepository;
import com.agi.aesl.erpscm.cs.repository.CsAccountVAHistoryRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsAccountServiceImpl implements CsAccountService{

    private static final Integer PAGE_SIZE = 20;

    private final ClaimResolver claimResolver;


    private final CsAccountRepository csAccountRepository;


    private final CsAccountVAHistoryRepository csAccountVAHistoryRepo;


    private final UserApplicationValidatorService<CsAccount> verificationService;

    private final CommentService commentService;

    private static final String DATE_TIME_END="23:59:59";

    private LocalDateTime parseDate(Optional<String> dateStr,String endTime){
        LocalDateTime date = null;
        if(dateStr.isPresent()){
            String time = (endTime!=null && endTime.trim().length()==8)? "T"+endTime:"T00:00:00";
            date = LocalDateTime.parse(dateStr.get()+time);
        }
        return date;
    }

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
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new AesException("Sorry! no employee profile found");
        }
        Employee employee = empOp.get();
        Optional<CsAccount> csAccountOp = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setCsType(acsUpdateDto.getCsType());
            csAccount.setVatType(acsUpdateDto.getVatType());
            csAccount.setRequestedBy(employee);
            csAccount.setDeliveryValuationMethod(acsUpdateDto.getDeliveryValuationMethod());
            List<String> ids= new ArrayList<>();
            csAccount.getCs().getCsDetails().forEach(csd->{
                ids.add(csd.getIndentDetail().getSubCategory().getId().toString());
                ids.add(csd.getIndentDetail().getSubCategory().getParentCategory().getId().toString());
            });
            csAccountVAHistoryRepo.deleteByCsAccountId(csAccount.getId());
            verificationService.removeVerification(csAccount.getId(),DomainType.ACS);
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
            setVAHistory(cs, verification.getVerifier(),CsStatus.VERIFIED);
        }
    }

    @Transactional
    private void setVAHistory(CsAccount csAccount, Employee employee, CsStatus status){
        CsAccountVAHistory csVaHistory = new CsAccountVAHistory();
        csVaHistory.setCsAccount(csAccount);
        csVaHistory.setEmployee(employee);
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
            setVAHistory(csAccount,verification.getVerifier(), CsStatus.APPROVED);
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
            setVAHistory(csAccount,new Employee(csAccount.getNextVerifierId()),CsStatus.VERIFIED);

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<CsAccount> csAccountOp  = csAccountRepository.findById(id);
        if(csAccountOp.isPresent()){
            CsAccount csAccount = csAccountOp.get();
            csAccount.setAcsStatus(CsStatus.APPROVED);
            setVAHistory(csAccount,new Employee(csAccount.getNextApproverId()),CsStatus.APPROVED);

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

        Optional<CsAccount> csAccountOp = csAccountRepository.findById(domainId);
        if(csAccountOp.isEmpty()){
            throw new AesException("Sorry! Account Cs  not found");
        }

        CsAccount csAccount = csAccountOp.get();
        csAccount.setReviewerId(null);
        csAccount.setReviewDate(null);
        csAccount.setAcsStatus(CsStatus.REJECTED);
        csAccount.setReviewPrevStatus(null);

        commentService.addComment(
                commentService.prepareComment(verifier,
                        DomainType.ACS,csAccount.getId(),rejectDto.getComment(),rejectDto.getAttachments())
        );
    }
    
    

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getPendingAcs(Jwt token,
                                                                  Optional<String> indentNo, Optional<String> status,
                                                                  Optional<String> fromDateStr, Optional<String> toDateStr,
                                                                  Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = Arrays.asList(
                CsStatus.PENDING.name(),
                CsStatus.PENDING_VERIFICATION.name(),
                CsStatus.PENDING_APPROVAL.name(),
                CsStatus.REVIEW.name()
        );
        if(status.isPresent()){
            csStatus = new ArrayList<>();
            csStatus.add(status.get());
        }
        return csAccountRepository.findAllAcs(indentNo, csStatus, fromDate,toDate,  pageable);
    }

    private static Pageable getPageable(Optional<Integer> page, Optional<Integer> sizeOp) {
        if(page.isEmpty()){
            throw new AesException("Sorry! Page number Required");
        }
        if(sizeOp.isEmpty()){
            throw new AesException("Sorry! Page Size Required");
        }
        Integer size = (sizeOp.get().equals(-1))? Integer.MAX_VALUE: sizeOp.orElse(PAGE_SIZE);
        return PageRequest.of(page.orElse(0),size);

    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getPendingVerificationAcs(Jwt token, Optional<String> indentNo, Optional<String> status,
                                             Optional<String> fromDateStr,Optional<String> toDateStr,
                                             Optional<Integer> page,Optional<Integer> size
    ) {
        claimResolver.setToken(token);
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr, DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();
        csStatus.add(CsStatus.PENDING_VERIFICATION.toString());
        csStatus.add(CsStatus.REVIEW.toString());
        csStatus.add(CsStatus.VERIFIED.toString());
        return csAccountRepository.findAllPendingVerificationAcs(indentNo,
                claimResolver.getUserId(),csStatus,status.orElse(null),fromDate,toDate,pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getPendingApprovalAcs(Jwt token,
                                         Optional<String> indentNo, Optional<String> status,
                                         Optional<String> fromDateStr, Optional<String> toDateStr,
                                         Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();
        csStatus.add(CsStatus.PENDING_APPROVAL.toString());
        csStatus.add(CsStatus.REVIEW.toString());
        csStatus.add(CsStatus.APPROVED.toString());
        if(status.isPresent()){
            csStatus = new ArrayList<>();
            csStatus.add(status.get());
        }
        return csAccountRepository.findAllPendingApprovalAcs(indentNo,claimResolver.getUserId(), csStatus,
                fromDate, toDate,
                pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getApprovedAcs(Jwt token,
                                  Optional<String> indentNo, Optional<String> status,
                                  Optional<String> fromDateStr, Optional<String> toDateStr,
                                  Optional<Integer> page,Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();
        csStatus.add(CsStatus.APPROVED.toString());
        csStatus.add(CsStatus.COMPLETED.toString());
        csStatus.add(CsStatus.VERIFIED.toString());
        if(status.isPresent()){
            csStatus=new ArrayList<>();
            csStatus.add(status.get());
        }
        return csAccountRepository.findAllAcs(indentNo, csStatus,fromDate, toDate, pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getRejectedAcs(Jwt token,
                                  Optional<String> indentNo, Optional<String> status,
                                  Optional<String> fromDateStr, Optional<String> toDateStr,
                                  Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();
        csStatus.add(CsStatus.REJECTED.toString());
        if(status.isPresent()){
            csStatus=new ArrayList<>();
            csStatus.add(status.get());
        }
        return csAccountRepository.findAllAcs(indentNo, csStatus,fromDate, toDate,pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getClosedAcs(Jwt token,
                                Optional<String> indentNo, Optional<String> status,
                                Optional<String> fromDateStr, Optional<String> toDateStr,
                                Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();
        if(status.isEmpty()) {
            csStatus.add(CsStatus.APPROVED.toString());
            csStatus.add(CsStatus.VERIFIED.toString());
            csStatus.add(CsStatus.REJECTED.toString());
            csStatus.add(CsStatus.COMPLETED.toString());
        }
        if(status.isPresent()){
            csStatus=new ArrayList<>();
            csStatus.add(status.get());
        }
        return csAccountRepository.findAllAcs(indentNo, csStatus, fromDate,toDate,pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getActiveCsList(Jwt token, Optional<String> indentNo,
                                   Optional<Long> categoryId,
                                   Optional<Long> subCategoryId,
                                   Optional<String> fromDateStr,
                                   Optional<String> toDateStr,
                                   Optional<Integer> page,
                                   Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        LocalDateTime fromDate = parseDate(fromDateStr,null);
        LocalDateTime toDate = parseDate(toDateStr,DATE_TIME_END);
        List<String> csStatus = new ArrayList<>();

        csStatus.add(CsStatus.APPROVED.toString());
        csStatus.add(CsStatus.VERIFIED.toString());
        csStatus.add(CsStatus.COMPLETED.toString());
        return csAccountRepository.findAllActiveCs(indentNo.orElse(null),csStatus,
                categoryId.orElse(null),subCategoryId.orElse(null),
                fromDate,toDate,pageable);
    }

    @Override
    public Page<CsAccountRepository.AcsPendingItem> getExpiredCsList(Jwt token, Optional<String> indentNo,
                                    Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = getPageable(page, size);
        List<String> csStatus = new ArrayList<>();

        csStatus.add(CsStatus.APPROVED.toString());
        csStatus.add(CsStatus.VERIFIED.toString());
        csStatus.add(CsStatus.COMPLETED.toString());
        return csAccountRepository.findAllExpiredCs(indentNo.orElse(null),csStatus,pageable);
    }

    @Override
    @Transactional
    public void reviewAcs(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<CsAccount> csAccountOp = csAccountRepository.findById(id);
        if(csAccountOp.isEmpty()){
            throw new AesException("Sorry! Account Cs  not found");
        }

        CsAccount csAccount = csAccountOp.get();
        csAccount.setReviewerId(null);
        csAccount.setReviewDate(LocalDateTime.now());
        if(csAccount.getReviewPrevStatus()!=null) {
            csAccount.setAcsStatus(csAccount.getReviewPrevStatus());
        }
        csAccount.setReviewPrevStatus(null);

        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),
                        DomainType.ACS,csAccount.getId(),noteDto.getNote(),noteDto.getAttachments())
        );
    }

    @Override
    public Optional<Map<String,Object>> getDetailById(Long id) {
        Optional<CsAccount> csAccountOp = csAccountRepository.findById(id);
        if(csAccountOp.isEmpty()){
            throw new AesException("Sorry! Account Cs not found");
        }

        CsAccount csAccount = csAccountOp.get();
        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(DomainType.ACS, csAccount.getId());
        vrs.forEach(verifier->{
            if(Boolean.FALSE.equals(verifier.getIsApproval())){
                verifiers.add(verifier);
            }else{
                approvers.add(verifier);
            }
        });

        List<?> comments = commentService.getCommentsByDomain(DomainType.ACS, csAccount.getId());
        Map<String,Object> result = new HashMap<>();
        result.put("csId",csAccount.getCs().getId());
        result.put("requestedBy",csAccount.getRequestedBy());
        result.put("status",csAccount.getAcsStatus());
        result.put("csType",csAccount.getCsType());
        result.put("vatType",csAccount.getVatType());
        result.put("deliveryValuationMethod",csAccount.getDeliveryValuationMethod());
        result.put("verifiers",verifiers);
        result.put("approvers",approvers);
        result.put("comments",comments);
        return Optional.of(result);
    }
}
