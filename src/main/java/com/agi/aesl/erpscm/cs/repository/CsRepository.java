package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.Cs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CsRepository extends JpaRepository<Cs,Long>, CsQuery {

    @Query(value = fetchLockedVendorsByItem,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(@Param("tenderId") Long tenderId, @Param("itemName")String itemName);

    @Query(value = fetchLockedVendorsByVendorAndItem,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(Long tenderId, Long vendorId, String itemName);

    @Query(value = pendingVerifications,countQuery = countPendingVerifications, nativeQuery = true)
    Page<?> findPendingVerificationCs(String nextVerifierId, Pageable pageable);

    @Query(value = pendingApprovals,countQuery = countPendingApprovals, nativeQuery = true)
    Page<?> findPendingApprovalCs(String nextApproverId, Pageable pageable);

    @Query(value = closedCs,countQuery = countClosedCs, nativeQuery = true)
    Page<?> findClosedCs(Pageable pageable);

    @Query(value = approvedCs,countQuery = countApprovedCs, nativeQuery = true)
    Page<?> findApprovedCs(Pageable pageable);

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
