package com.agi.aesl.erpscm.account_finance.repository;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;

@FunctionalInterface
public interface UpdateLedgerVerifier {
    void updateVerifier(LedgerAccount ledgerAccount);
}
