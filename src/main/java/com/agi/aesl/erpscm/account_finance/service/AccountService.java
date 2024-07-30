package com.agi.aesl.erpscm.account_finance.service;

import com.agi.aesl.erpscm.account_finance.dto.request.LedgerAccountRequestDto;
import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface AccountService extends VerificationDomainService {
    String getNextAccountNo();

    Page<?> getAllPendingAccounts(Optional<Integer> page, Optional<Integer> size);

    Page<?> getAllPendingVerifications(Jwt token,
                                             Optional<Integer> page, Optional<Integer> size,
                                             Optional<String> fromDate, Optional<String> toDate
    );
    Page<?> getAllPendingApprovals(Jwt token,
                                         Optional<Integer> page, Optional<Integer> size,
                                         Optional<String> fromDate, Optional<String> toDate

    );

    Optional<?> getLedgerDetailById(Long id);

    Page<?> getAllApprovedAccounts(Optional<Integer> page, Optional<Integer> size);

    Page<?> getAllRejectedAccounts(Optional<Integer> page, Optional<Integer> size);

    void updateAccount(Jwt token, String uri, Long id, LedgerAccountRequestDto ledgerAccountRequestDto);
}
