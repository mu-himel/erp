package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.Cs;
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
