package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.agi.aesl.erpscm.cs.repository.AcsQuery.*;

@Repository
public interface CsAccountRepository extends JpaRepository<CsAccount,Long> {
    @Query(value = GET_ALL_ACS_BY_INDENT_NO_AND_STATUS, countQuery = COUNT_BY_INDENT_NO_AND_STATUS_ACS, nativeQuery = true)
    Page<AcsPendingItem> findAllAcs(Optional<String> indentNo, List<String> status,
                                    LocalDateTime fromDate, LocalDateTime toDate,
                                    Pageable pageable);

    @Query(value = GET_ALL_PV_ACS_BY_INDENT_NO_AND_STATUS, countQuery = COUNT_ALL_PV_ACS_BY_INDENT_NO_AND_STATUS, nativeQuery = true)
    Page<AcsPendingItem> findAllPendingVerificationAcs(Optional<String> indentNo,
                                                       String nextVerifierId, List<String> status,
                                                       LocalDateTime fromDate,
                                                       LocalDateTime toDate,
                                                       Pageable pageable);

    @Query(value = GET_ALL_PA_ACS_BY_INDENT_NO_AND_STATUS, countQuery = COUNT_ALL_PA_ACS_BY_INDENT_NO_AND_STATUS, nativeQuery = true)
    Page<AcsPendingItem> findAllPendingApprovalAcs(Optional<String> indentNo,String nextApproverId,
                                                   List<String> status,
                                                   LocalDateTime fromDate, LocalDateTime toDate,
                                                   Pageable pageable);

    @Query(value = GET_ALL_ACTIVE_CS_BY_INDENT_NO, countQuery = COUNT_ALL_ACTIVE_CS_BY_INDENT_NO, nativeQuery = true)
    Page<AcsPendingItem> findAllActiveCs(String indentNo, List<String> status,
                                         Long categoryId, Long subCategoryId,
                                         LocalDateTime fromDate, LocalDateTime toDate,
                                         Pageable pageable);

    @Query(value = GET_ALL_EXPIRED_CS_BY_INDENT_NO, countQuery = COUNT_ALL_EXPIRED_CS_BY_INDENT_NO, nativeQuery = true)
    Page<AcsPendingItem> findAllExpiredCs(String indentNo, List<String> status, Pageable pageable);

    Optional<CsAccount> findByCsId(Long id);

    interface AcsPendingItem extends CsRepository.CsPendingListInfo{
        Long getAcsId();
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        LocalDate getValidityDate();
    }
}
