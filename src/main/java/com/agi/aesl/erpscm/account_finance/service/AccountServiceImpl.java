package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.account_finance.repository.AccountRepository;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService{

    private static final Integer PAGE_SIZE = 10;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private IntegrationWriterService integrationWriterService;

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
//            Item item = ledgerAccount.getItem();
//            String itemAttributeName = item.getItemAttributeName();
//            String brandName = item.getName();
//            integrationWriterService.createLedgerAccount();
        }
    }
}
