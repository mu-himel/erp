package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService{

    private static final Integer PAGE_SIZE = 10;
    @Autowired
    private AccountRepository accountRepository;

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
        // accountRepository.findBy
        return null;
    }

    @Override
    public Page<?> getAllRejectedAccounts(Optional<Integer> page, Optional<Integer> size) {
        //TODO for Sourav
        return null;
    }
}
