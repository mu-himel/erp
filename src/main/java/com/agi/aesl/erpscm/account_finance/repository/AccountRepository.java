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

    @Query(value = GET_PENDING_ACCOUNTS, countQuery = COUNT_PENDING_ACCOUNTS, nativeQuery = true)
    Page<PendingAccount> getPendingLedgerAccounts(List<Long> warehouseId, Pageable pageable);

    @Query(value =LEDGER_ACC_DETAIL ,nativeQuery = true)
    Optional<PendingAccountDetail> findLedgerAccountById(Long id);

    @Query(value = GET_APPROVED_ACCOUNTS,countQuery = COUNT_APPROVED_ACCOUNTS, nativeQuery = true)
    Page<PendingAccount> getApprovedLedgerAccounts(List<Long> warehouseId,Pageable pageable);

    @Query(value = GET_REJECTED_ACCOUNTS,countQuery = COUNT_REJECTED_ACCOUNTS, nativeQuery = true)
    Page<PendingAccount> getRejectedLedgerAccounts(List<Long> warehouseId,Pageable pageable);

    @Query(value = GET_ALL_FILTERED_PV_WITH_NEXT_VERIFIER ,
            countQuery = COUNT_ALL_FILTERED_PV_WITH_NEXT_VERIFIER,nativeQuery = true)
    Page<PendingAccount> findAllByCategoryAndAccountStatusAndNextVerifierId(List<Long> categoryIds,
                                                                            String nextVerifierId,
                                                                            List<String> accountStatuses,
                                                                            LocalDateTime fromDate,
                                                                            LocalDateTime toDate,
                                                                            Pageable pageable);

    @Query(value = GET_ALL_FILTERED_PV,
            countQuery = COUNT_ALL_FILTERED_PV,nativeQuery = true)
    Page<PendingAccount> findAllByAccountStatusAndNextVerifierId(List<String> accountStatuses,
                                                                 String nextVerifierId,
                                                                 LocalDateTime fromDate,
                                                                 LocalDateTime toDate,
                                                                 Pageable pageable);
    @Query(value = GET_ALL_FILTERED_PA_WITH_NEXT_APPROVER,
            countQuery = COUNT_ALL_FILTERED_PA_WITH_NEXT_APPROVER,nativeQuery = true)
    Page<PendingAccount> findAllByCategoryAndAccountStatusAndNextApproverId(List<Long> categoryIds,
                                                                            String nextApproverId,
                                                                            List<String> accountStatuses,
                                                                            LocalDateTime fromDate,
                                                                            LocalDateTime toDate,
                                                                            Pageable pageable);
    @Query(value = GET_ALL_FILTERED_PA,
            countQuery = COUNT_ALL_FILTERED_PA,nativeQuery = true)
    Page<PendingAccount> findAllByAccountStatusAndNextApproverId(List<String> accountStatuses,
                                                                 String nextApproverId,
                                                                 LocalDateTime fromDate,
                                                                 LocalDateTime toDate,
                                                                 Pageable pageable);

    @Query(value = GET_CLOSED_ACCOUNTS, countQuery = COUNT_CLOSE_ACCOUNTS ,nativeQuery = true)
    Page<PendingAccount> getClosedLedgerAccounts(List<Long> warehouseId, Pageable pageable);
}
