package com.agi.aesl.erpscm.account_finance.service;

import org.springframework.data.domain.Page;

import java.util.Optional;

public interface AccountService {
    String getNextAccountNo();

    Page<?> getAllPendingAccounts(Optional<Integer> page, Optional<Integer> size);
}
