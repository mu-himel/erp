package com.agi.aesl.erpscm.account_finance.repository;

import com.agi.aesl.erpscm.account_finance.entity.LedgerAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<LedgerAccount,Long> , AccountQuery{

    @Query(value = "SELECT max(la.id) FROM LedgerAccount la")
    Optional<Long> findMaxOrderById();

    @Query(value = getPendingAccounts, countQuery = countPendingAccounts, nativeQuery = true)
    Page<PendingAccount> getPendingLedgerAccounts(List<Long> warehouseId, Pageable pageable);

    @Query(value =ledgerAccDetail ,nativeQuery = true)
    Optional<PendingAccountDetail> findLedgerAccountById(Long id);

    @Query(value = getApprovedAccountsList,countQuery = countApprovedAccounts, nativeQuery = true)
    Page<PendingAccount> getApprovedLedgerAccounts(List<Long> warehouseId,Pageable pageable);

    @Query(value = getRejectedAccountsList,countQuery = countRejectedAccounts, nativeQuery = true)
    Page<PendingAccount> getRejectedLedgerAccounts(List<Long> warehouseId,Pageable pageable);

    @Query(value = getAllFilteredPendingVerificationsWithNextVerifier,
            countQuery = countAllFilteredPendingVerificationsWithNextVerifier,nativeQuery = true)
    Page<PendingAccount> findAllByCategoryAndAccountStatusAndNextVerifierId(List<Long> categoryIds,
                                                                            String nextVerifierId,
                                                                            List<String> accountStatuses,
                                                                            LocalDateTime fromDate,
                                                                            LocalDateTime toDate,
                                                                            Pageable pageable);

    @Query(value = getAllFilteredPendingVerifications,
            countQuery = countAllFilteredPendingVerifications,nativeQuery = true)
    Page<PendingAccount> findAllByAccountStatusAndNextVerifierId(List<String> accountStatuses,
                                                                 String nextVerifierId,
                                                                 LocalDateTime fromDate,
                                                                 LocalDateTime toDate,
                                                                 Pageable pageable);
    @Query(value = getAllFilteredPendingApprovalsWithNextApprover,
            countQuery = countAllFilteredPendingApprovalsWithNextApprover,nativeQuery = true)
    Page<PendingAccount> findAllByCategoryAndAccountStatusAndNextApproverId(List<Long> categoryIds,
                                                                            String nextApproverId,
                                                                            List<String> accountStatuses,
                                                                            LocalDateTime fromDate,
                                                                            LocalDateTime toDate,
                                                                            Pageable pageable);
    @Query(value = getAllFilteredPendingApprovals,
            countQuery = countAllFilteredPendingApprovals,nativeQuery = true)
    Page<PendingAccount> findAllByAccountStatusAndNextApproverId(List<String> accountStatuses,
                                                                 String nextApproverId,
                                                                 LocalDateTime fromDate,
                                                                 LocalDateTime toDate,
                                                                 Pageable pageable);

    @Query(value = getClosedAccounts, countQuery = countClosedAccounts ,nativeQuery = true)
    Page<PendingAccount> getClosedLedgerAccounts(List<Long> warehouseId, Pageable pageable);
}
