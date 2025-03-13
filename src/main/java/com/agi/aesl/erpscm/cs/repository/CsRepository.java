package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.Cs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.agi.aesl.erpscm.cs.repository.CsQuery.*;

@Repository
public interface CsRepository extends JpaRepository<Cs,Long> {

    @Query(value = FETCH_LOCKED_VENDORS_BY_ITEM,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(@Param("tenderId") Long tenderId,
                                                           @Param("brandName") String brandName,
                                                           @Param("itemName")String itemName);

    @Query(value = FETCH_LOCKED_VENDORS_BY_VENDOR_AND_ITEM,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(Long tenderId, Long vendorId, String itemName);

    @Query(value = PENDING_VERIFICATIONS,countQuery = COUNT_PENDING_VERIFICATIONS, nativeQuery = true)
    Page<CsPendingListInfo> findPendingVerificationCs(String nextVerifierId,
                                                      String indentNo, List<String> statuses,
                                                      LocalDateTime fromDate, LocalDateTime toDate,
                                                      Pageable pageable);

    @Query(value = PENDING_APPROVALS,countQuery = COUNT_PENDING_APPROVALS, nativeQuery = true)
    Page<CsPendingListInfo> findPendingApprovalCs(String nextApproverId,
                                                  String indentNo, List<String> statuses,
                                                  LocalDateTime fromDate, LocalDateTime toDate,
                                                  Pageable pageable);

    @Query(value = CLOSED_CS,countQuery = COUNT_CLOSED_CS, nativeQuery = true)
    Page<CsPendingListInfo> findClosedCs(String indentNo, List<String> statuses,
                                         LocalDateTime fromDate, LocalDateTime toDate,
                                         Pageable pageable);

    @Query(value = APPROVED_CS,countQuery = COUNT_APPROVED_CS, nativeQuery = true)
    Page<CsPendingListInfo> findApprovedCs(
            String indentNo, List<String> statuses,
            LocalDateTime fromDate, LocalDateTime toDate,
            Pageable pageable);

    @Query(value=GET_POTENTIAL_PO_LIST_FROM_CS,nativeQuery = true)
    List<PotentialPoListItem> getPotentialPoListFromCs(Long csId);

    interface PotentialPoListItem {
        Long getCvddId();
        String getCsVendorDetailId();
        String getItemAttribute();
        Long getPriceQuotationId();
        Long getIndentDetailId();
        Long getVendorId();
        Long getCsDetailId();
        LocalDate getDeliveryDate();
        BigDecimal getDeliveryQty();
        Long getWarehouseId();

    }
    interface CsPendingListInfo {
        Long getId();
        Long getCsId();
        LocalDateTime getCsDate();
        String getCsNo();
        String getCategoryName();
        Long getItems();
        Long getRfqQty();
        Long getLockedVendor();
        String getStatus();
    }

    interface ItemWiseVendorDetail{
        Long getPqId();
        Long getVendorId();
        BigDecimal getRfqQty();
        Integer getScore();
        String getBrandName();
        String getExtendedAttributes();
        String getItemAttribute();
        BigDecimal getTotalPrice();
        BigDecimal getUnitPrice();
        Long getEstDeliveryDays();
        String getVendorName();
        String getVendorEmail();
        String getVendorPhoneNo();
        String getVendorType();
        Long getRfqId();
        String getStatus();
        String getPriceQuotationStatus();
        String getCreditPaymentUnit();
        Integer getCreditPaymentDuration();
        String getDeliveryCharge();
        BigDecimal getDeliveryChargeAmount();
        Boolean getIsVatAdded();
        Boolean getIsAitAdded();
        BigDecimal getVatPercent();
        BigDecimal getVatAmount();
        BigDecimal getSubTotalPrice();
        BigDecimal getTotalPriceInSummary();
        String getPaymentMethod();
        Integer getWarrantyDuration();
        String getWarrantyUnit();
    }
}
