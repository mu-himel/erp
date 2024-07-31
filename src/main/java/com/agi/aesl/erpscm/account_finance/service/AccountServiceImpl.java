package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.dto.request.RemoteLedgerAccountDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccountVerifyApprovalHistory;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.account_finance.repository.AccountQuery;
import com.agi.aesl.erpscm.account_finance.repository.AccountRepository;
import com.agi.aesl.erpscm.account_finance.repository.AccountVerificationApprovalRepository;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandVerificationApprovalHistory;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.modules.dto.VerifierInfo;
import com.agi.aesl.erpscm.modules.service.ModuleService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService{

    private static final Integer PAGE_SIZE = 10;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private IntegrationWriterService integrationWriterService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserApplicationValidatorService<LedgerAccount> verificationService;

    @Autowired
    private AccountVerificationApprovalRepository accountVerificationApprovalRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Override
    public String getNextAccountNo() {
        Optional<Long> accountNoOp = accountRepository.findMaxOrderById();
        if(accountNoOp.isPresent()){
            Long accountNo = accountNoOp.get();
            Long newAccountNo = accountNo + 1;
            return "A-"+String.format("%05d",newAccountNo);
        }
        return "A-"+String.format("%05d",1);
    }



    @Override
    public Page<?> getAllPendingAccounts(Optional<Integer> page, Optional<Integer> size) {

        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        return accountRepository.getPendingLedgerAccounts(pageable);
    }

    @Override
    public Page<?> getClosedAccounts(Optional<Integer> page, Optional<Integer> size) {
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        return accountRepository.getClosedLedgerAccounts(pageable);
    }

    @Override
    public Page<?> getAllPendingVerifications(Jwt token, Optional<Integer> page,
                                              Optional<Integer> size,
                                              Optional<String> fromDateStr,
                                              Optional<String> toDateStr) {
        claimResolver.setToken(token);
        String moduleUri = "/app/accounts/asset-management/asset-ledger/pending-verification";
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10));
        Optional<Map<String,List<Long>>> modulePermission = integrationReaderService
                .getModuleFilterByUri(token, moduleUri);

        List<Long> categoryIds = new ArrayList<>();

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+"T00:00:00");
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+"T23:59:59");
        }
        List<String> demandStatuses = new ArrayList<>();
        demandStatuses.add(DemandStatus.PENDING_VERIFICATION.toString());
        demandStatuses.add(DemandStatus.REVIEW.toString());

        if(modulePermission.isPresent()){
            categoryIds = modulePermission.get().get("category_id");
            return accountRepository.findAllByCategoryAndAccountStatusAndNextVerifierId(categoryIds,
                    claimResolver.getUserId(),
                    demandStatuses,
                    fromDate,toDate,
                    pageable);
        }

        return accountRepository.findAllByAccountStatusAndNextVerifierId(
                demandStatuses,
                claimResolver.getUserId(),
                fromDate,toDate,
                pageable);
    }

    @Override
    public Page<?> getAllPendingApprovals(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                          Optional<String> fromDateStr, Optional<String> toDateStr) {
        claimResolver.setToken(token);
        String moduleUri = "demand/pending-approval";
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10));
        Optional<Map<String,List<Long>>> modulePermission = integrationReaderService
                .getModuleFilterByUri(token, moduleUri);

        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateStr.isPresent()){
            fromDate = LocalDateTime.parse(fromDateStr.get()+"T00:00:00");
        }
        if(toDateStr.isPresent()){
            toDate = LocalDateTime.parse(toDateStr.get()+"T23:59:59");
        }

        List<String> demandStatuses = new ArrayList<>();
        demandStatuses.add(DemandStatus.PENDING_APPROVAL.toString());
        demandStatuses.add(DemandStatus.REVIEW.toString());
        List<Long> categoryIds = new ArrayList<>();

        if(modulePermission.isPresent()){
            categoryIds = modulePermission.get().get("category_id");
            return accountRepository.findAllByCategoryAndAccountStatusAndNextApproverId(categoryIds,
                    claimResolver.getUserId(),
                    demandStatuses,
                    fromDate,toDate,
                    pageable);
        }
        return accountRepository.findAllByAccountStatusAndNextApproverId(
                demandStatuses,
                claimResolver.getUserId(),
                fromDate,toDate,
                pageable);


    }

    private void setVerifiers(LedgerAccount ledgerAccount, List<VerifierInfo> verifiers) {
        if(verifiers.size()>0){
            Optional<VerifierInfo> firstOp = verifiers.stream().findFirst();
            VerifierInfo _verifier = firstOp.get();

//            demandMailService.prepareMailContent(_verifier.getName(), "Verification", demand);
//            demandMailService.sentMail(_verifier.getEmail(),"Pending Demand Verification Request");

            List<UserApplicationValidation> verifications = verifiers.stream().map(verifier -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(ledgerAccount.getId());
                verification.setDomainType(DomainType.ACCOUNT_LEDGER);
                verification.setVerified(false);
                verification.setIsApproval(false);
                verification.setVerifier(new Employee(verifier.getId()));
                return verification;
            }).collect(Collectors.toList());
            ledgerAccount.setNextVerifierId(_verifier.getId());
            verificationService.addVerification(verifications);
        }
    }

    private void setApprovers(LedgerAccount ledgerAccount, List<ApprovalPanel> approvalPanels) {
        if(approvalPanels.size()>0){
            List<UserApplicationValidation> verifications = approvalPanels.stream().map(approvalPanel -> {
                UserApplicationValidation verification = new UserApplicationValidation();
                verification.setDomainId(ledgerAccount.getId());
                verification.setDomainType(DomainType.ACCOUNT_LEDGER);
                verification.setVerified(false);
                verification.setIsApproval(true);
                verification.setVerifier(new Employee(approvalPanel.getUserId()));
                return verification;
            }).collect(Collectors.toList());

            verificationService.addVerification(verifications);
        }
    }

    private List<VerifierInfo> getVerifiers(LedgerAccount ledgerAccount, Optional<VerifierConfig> verifierOp) {
        List<VerifierInfo> verifiers = new ArrayList<>();
        if(verifierOp.isPresent()){
            VerifierConfig verification = verifierOp.get();
            verifiers = verification.getVerifiers();
            Boolean verificationRequired = verification.getVerificationRequired();
            if(verificationRequired!=null && verificationRequired==true && verifiers!=null && verifiers.size()>0){
                ledgerAccount.setAccountStatus(AccountType.PENDING_VERIFICATION);
            }else{
                ledgerAccount.setAccountStatus(AccountType.PENDING);
            }

        }else{
            ledgerAccount.setAccountStatus(AccountType.PENDING);
        }
        return verifiers;
    }

    @Override
    public Optional<?> getLedgerDetailById(Long id) {
        Optional<AccountQuery.PendingAccountDetail>  accountOp = accountRepository.findLedgerAccountById(id);
        if(accountOp.isPresent()){

            AccountQuery.PendingAccountDetail pendingAccount = accountOp.get();
            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.ACCOUNT_LEDGER, pendingAccount.getId())
                    .stream().forEach(verifier->{
                        if(verifier.getIsApproval()==false){
                            verifiers.add(verifier);
                        }else{
                            approvers.add(verifier);
                        }
                    });
            Map<String,Object> resDto = new HashMap<>();

            Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(pendingAccount.getInitiatorWarehouseId());
            resDto.put("warehouseName:",warehouseOp.isPresent()? warehouseOp.get().getName():"");
            resDto.put("warehouseLocation:",warehouseOp.isPresent()? warehouseOp.get().getLocation():"");
            resDto.put("detail",pendingAccount);
            resDto.put("verifiers",verifiers);
            resDto.put("approvers",approvers);


            List<?> comments = commentService.getCommentsByDomain(DomainType.ACCOUNT_LEDGER, pendingAccount.getId());
            resDto.put("comments",comments);

            return Optional.ofNullable(resDto);
        }


        return Optional.empty();

    }

    @Override
    public Page<?> getAllApprovedAccounts(Optional<Integer> page, Optional<Integer> size) {
        //TODO for Sourav
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return accountRepository.getApprovedLedgerAccounts(pageable);
    }

    @Override
    public Page<?> getAllRejectedAccounts(Optional<Integer> page, Optional<Integer> size) {
        //TODO for Sourav
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return accountRepository.getRejectedLedgerAccounts(pageable);
    }

    @Override
    @Transactional
    public void updateAccount(Jwt token, String uri, Long id, LedgerAccountRequestDto ledgerAccountRequestDto) {
        claimResolver.setToken(token);
        Optional<Employee> employeeOp = claimResolver.getEmployee();
        Employee employee = employeeOp.orElse(null);


        Optional<LedgerAccount> update_data = accountRepository.findById(id);
        if(update_data.isPresent()){
            LedgerAccount ledgerAccount = update_data.get();
            ledgerAccount.setGroupAccount(ledgerAccountRequestDto.getGroupAccount());
            ledgerAccount.setMasterAccount(ledgerAccountRequestDto.getMasterAccount());
            ledgerAccount.setSubGroupAccount(ledgerAccountRequestDto.getSubGroupAccount());
            ledgerAccount.setStore(ledgerAccountRequestDto.getStore());
            ledgerAccount.setOpeningDate(LocalDate.parse(ledgerAccountRequestDto.getOpeningDate()));
            ledgerAccount.setOpeningCreditAmount(ledgerAccountRequestDto.getOpeningCreditAmount());
            ledgerAccount.setOpeningDebitAmount(ledgerAccountRequestDto.getOpeningDebitAmount());
            ledgerAccount.setRequestedBy(employee);
            ledgerAccount.setAccountStatus(AccountType.PENDING_VERIFICATION);
            accountRepository.save(ledgerAccount);

            List<String> ids = new ArrayList<>();
            ids.add(ledgerAccountRequestDto.getCategoryId().toString());
            ids.add(ledgerAccountRequestDto.getSubCategoryId().toString());

            Optional<VerifierConfig> verifierOp = verificationService.getVerifiers(claimResolver,uri,
                    "CATEGORY",String.join(",",ids));

            List<VerifierInfo> verifiers = getVerifiers(ledgerAccount, verifierOp);
            List<ApprovalPanel> panels = getApprovalPanels(claimResolver, uri, String.join(",",ids));

            verificationService.setVerifiers(ledgerAccount,verifiers,DomainType.ACCOUNT_LEDGER);
            if(verifiers.size()==0 && panels.size()>0){
                ledgerAccount.setAccountStatus(AccountType.PENDING_APPROVAL);
                Optional<ApprovalPanel> firstPanel = panels.stream().findFirst();
                if(firstPanel.isPresent()){
                    ApprovalPanel panel = firstPanel.get();
//                    demandMailService.prepareMailContent(panel.getName(), "Approval", demand);
//                    demandMailService.sentMail(panel.getEmail(),"Pending Demand Approval Request");
                    ledgerAccount.setNextApproverId(panel.getUserId());
                }
            }
            verificationService.setApprovers(ledgerAccount, panels,DomainType.ACCOUNT_LEDGER);

        }
    }

    private List<ApprovalPanel> getApprovalPanels(ClaimResolver claimResolver,String uri, String categories) {
        List<ApprovalPanel> approvalPanels = moduleService.getModuleWiseApprovalSetting(claimResolver,uri,
                Optional.ofNullable(categories),Optional.empty());
        return approvalPanels;
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<LedgerAccount> ledgerAccountOp  = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()){
            LedgerAccount ledgerAccount = ledgerAccountOp.get();

//            demandMailService.prepareMailContent(nextVerifier.getVerifier().getEmployeeName(),"Verification",demand);
//            demandMailService.sentMail(nextVerifier.getVerifier().getEmailAddress(),"Pending Demand Verification Request");


            LedgerAccountVerifyApprovalHistory ledgerAccountVerifyApprovalHistory = new LedgerAccountVerifyApprovalHistory();
            ledgerAccountVerifyApprovalHistory.setLedgerAccount(ledgerAccount);
            ledgerAccountVerifyApprovalHistory.setEmployee(verification.getVerifier());
            ledgerAccountVerifyApprovalHistory.setAccountStatus(AccountType.VERIFIED);
            accountVerificationApprovalRepository.save(ledgerAccountVerifyApprovalHistory);
            ledgerAccount.setNextVerifierId(nextVerifier.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<LedgerAccount> ledgerAccountOp  = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()){
            LedgerAccount ledgerAccount = ledgerAccountOp.get();
//            demandMailService.prepareMailContent(verificationResponse.getVerifier().getEmployeeName(),"Approval",demand);
//            demandMailService.sentMail(verificationResponse.getVerifier().getEmailAddress(),"Pending Demand Approval Request");

            LedgerAccountVerifyApprovalHistory ledgerAccountVAHistory = new LedgerAccountVerifyApprovalHistory();
            ledgerAccountVAHistory.setLedgerAccount(ledgerAccount);
            ledgerAccountVAHistory.setEmployee(verification.getVerifier());
            ledgerAccountVAHistory.setAccountStatus(AccountType.APPROVED);
            accountVerificationApprovalRepository.save(ledgerAccountVAHistory);
            ledgerAccount.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<LedgerAccount> ledgerAccountOp = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()){
            LedgerAccount ledgerAccount = ledgerAccountOp.get();

            LedgerAccountVerifyApprovalHistory ledgerAccountVAHistory = new LedgerAccountVerifyApprovalHistory();
            ledgerAccountVAHistory.setAccountStatus(AccountType.VERIFIED);
            ledgerAccountVAHistory.setLedgerAccount(ledgerAccount);
            ledgerAccountVAHistory.setEmployee(new Employee(ledgerAccount.getNextVerifierId()));
            accountVerificationApprovalRepository.save(ledgerAccountVAHistory);

            if(firstApprover.isPresent()){
//                demandMailService.prepareMailContent(firstApprover.get().getVerifier().getEmployeeName(),"Approval",demand);
//                demandMailService.sentMail(firstApprover.get().getVerifier().getEmailAddress(),"Pending Demand Approval Request");
                ledgerAccount.setNextApproverId(firstApprover.get().getVerifier().getId());
                ledgerAccount.setAccountStatus(AccountType.PENDING_APPROVAL);
            }else{
                ledgerAccount.setAccountStatus(AccountType.APPROVED);
            }


        }
    }

    @Override
    @Transactional
    public void onRejected(Long id) {
        Optional<LedgerAccount> ledgerAccountOp = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()){
            LedgerAccount ledgerAccount = ledgerAccountOp.get();
            ledgerAccount.setAccountStatus(AccountType.REJECTED);
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {

        Optional<LedgerAccount> ledgerAccountOp  = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()) {
            LedgerAccount ledgerAccount = ledgerAccountOp.get();
            ledgerAccount.setAccountStatus(AccountType.APPROVED);

            integrationWriterService.createLedgerItem(claimResolver.getToken(),ledgerAccount);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long id, RefDto reviewer, String comment) {
        Optional<LedgerAccount> ledgerAccountOp  = accountRepository.findById(id);
        if(ledgerAccountOp.isPresent()){
            LedgerAccount ledgerAccount = ledgerAccountOp.get();
            ledgerAccount.setReviewPrevStatus(ledgerAccount.getAccountStatus());
            ledgerAccount.setReviewerId(reviewer.getId());
            ledgerAccount.setAccountStatus(AccountType.REVIEW);
            ledgerAccount.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void createItemLedger(Item item) {
        LedgerAccount ledgerAccount = new LedgerAccount();
        ledgerAccount.setItem(item);
        ledgerAccount.setAccountNo(getNextAccountNo());
        ledgerAccount.setAccountStatus(AccountType.PENDING);
        accountRepository.save(ledgerAccount);
    }


}
