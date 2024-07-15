package com.agi.aesl.erpscm.account_finance.repository;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<LedgerAccount,Long> , AccountQuery{

    @Query(value = "SELECT max(la.id) FROM LedgerAccount la")
    Optional<Long> findMaxOrderById();

    @Query(value = getPendingAccounts, countQuery = countPendingAccounts, nativeQuery = true)
    Page<PendingAccount> getPendingLedgerAccounts(Pageable pageable);

    @Query(value =ledgerAccDetail ,nativeQuery = true)
    Optional<PendingAccountDetail> findLedgerAccountById(Long id);

    @Query(value = getApprovedAccountsList,countQuery = countApprovedAccounts, nativeQuery = true)
    Page<PendingAccount> getApprovedLedgerAccounts(Pageable pageable);

    @Query(value = getRejectedAccountsList,countQuery = countRejectedAccounts, nativeQuery = true)
    Page<PendingAccount> getRejectedLedgerAccounts(Pageable pageable);

}
