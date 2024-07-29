package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccountVerifyApprovalHistory;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.account_finance.repository.AccountRepository;
import com.agi.aesl.erpscm.account_finance.repository.AccountVerificationApprovalRepository;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.demand.dto.request.DemandRequestDto;
import com.agi.aesl.erpscm.demand.entity.Demand;
import com.agi.aesl.erpscm.demand.entity.DemandVerificationApprovalHistory;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.inventory.entity.Item;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        //TODO for Sourav
        return accountRepository.findLedgerAccountById(id);
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

            setVerifiers(ledgerAccount,verifiers);
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
            setApprovers(ledgerAccount, panels);
//
//            Item item = ledgerAccount.getItem();
//            String itemAttributeName = item.getItemAttributeName();
//            String brandName = item.getName();
//            integrationWriterService.createLedgerAccount();
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
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {

    }

    @Override
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

            }else{

            }


        }
    }

    @Override
    public void approveComplete(Long id) {

    }

    @Override
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {

    }
}
