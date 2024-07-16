package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface AccountService {
    String getNextAccountNo();

    Page<?> getAllPendingAccounts(Optional<Integer> page, Optional<Integer> size);

    Optional<?> getLedgerDetailById(Long id);

    Page<?> getAllApprovedAccounts(Optional<Integer> page, Optional<Integer> size);

    Page<?> getAllRejectedAccounts(Optional<Integer> page, Optional<Integer> size);

    void updateAccount(Jwt token, String uri, Long id, LedgerAccountRequestDto ledgerAccountRequestDto);
}
