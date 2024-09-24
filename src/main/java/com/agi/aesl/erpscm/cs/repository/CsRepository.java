package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.Cs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CsRepository extends JpaRepository<Cs,Long>, CsQuery {

    @Query(value = fetchLockedVendorsByItem,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(@Param("tenderId") Long tenderId, @Param("itemName")String itemName);

    @Query(value = fetchLockedVendorsByVendorAndItem,nativeQuery = true)
    List<ItemWiseVendorDetail> findLockedVendorsByItemName(Long tenderId, Long vendorId, String itemName);

    @Query(value = pendingVerifications,countQuery = countPendingVerifications, nativeQuery = true)
    Page<CsPendingListInfo> findPendingVerificationCs(String nextVerifierId, Pageable pageable);

    @Query(value = pendingApprovals,countQuery = countPendingApprovals, nativeQuery = true)
    Page<CsPendingListInfo> findPendingApprovalCs(String nextApproverId, Pageable pageable);

    @Query(value = closedCs,countQuery = countClosedCs, nativeQuery = true)
    Page<CsPendingListInfo> findClosedCs(Pageable pageable);

    @Query(value = approvedCs,countQuery = countApprovedCs, nativeQuery = true)
    Page<CsPendingListInfo> findApprovedCs(Pageable pageable);

    @Query(value="""
                SELECT cvdd.id as cvddId, 
                cvd.id as csVendorDetailId,
                    (
                        select item_attribute from indent_details ide where ide.id=cd.indent_detail_id 
                    ) as itemAttribute ,
                    cvd.price_quotation_id as priceQuotationId,
                    cd.indent_detail_id as indentDetailId, 
                    cvd.vendor_id as vendorId, 
                    cd.id as csDetailId, 
                    cvdd.delivery_date as deliveryDate, 
                    sum(cvdd.delivery_qty) as deliveryQty,
                    cvdd.warehouse_id as warehouseId 
                    From cs 
                    LEFT JOIN cs_details cd ON cd.cs_id = cs.id 
                    LEFT JOIN cs_vendor_details cvd ON cvd.cs_detail_id = cd.id
                    LEFT JOIN cs_vendor_delivery_details cvdd ON cvdd.vendor_delivery_detail_id = cvd.id
                    WHERE cs.id=:csId
                    GROUP BY cvd.vendor_id, cd.id, cvdd.delivery_date,cvdd.warehouse_id 
                """,nativeQuery = true)
    List<PotentialPoListItem> getPotentialPoListFromCs(Long csId);

    interface PotentialPoListItem {
        Long getCvddId();
        String getCsVendorDetailId();
        String getItemAttribute();
        Long getPriceQuotationId();
        Long getIndentDetailId();
        Long getVendorId();
        Long getCsDetailId();
        LocalDateTime getDeliveryDate();
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
