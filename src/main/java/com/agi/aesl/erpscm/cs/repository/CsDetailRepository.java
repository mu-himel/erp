package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.agi.aesl.erpscm.cs.repository.CsQuery.*;

@Repository
public interface CsDetailRepository extends JpaRepository<CsDetail, Long> {

    @Query(value = GET_ALL_ITEMS_BY_VENDOR, nativeQuery = true)
    List<CsVendorItemInfo> findAllItemsByVendor(String csNo, Long vendorId);

    @Query(value =GET_ALL_VENDOR_WAREHOUSE,nativeQuery = true)
    List<VendorWarehouseList> findAllVendorWarehouses(String csNo,Long vendorId);

    interface VendorWarehouseList{
        String getName();
        String getItemAttributeName();
        Long getWarehouseId();
        BigDecimal getPrQty();
        BigDecimal getRfqQty();
    }

    interface CsVendorItemInfo{
        String getBrandName();
        String getExtendedAttributes();
        String getItemAttribute();
        String getEstDeliveryDays();
        BigDecimal getUnitPrice();
        String getCsNo();
        Long getPqdId();
        Long getPqsId();
        Long getPqId();

        String getTransactionType();
        BigDecimal getDiscountAmount();
        BigDecimal getOrderQty();
        BigDecimal getRemainingQty();
        Long getDuration();
        String getDurationUnit();
        Boolean getIsAitAdded();
        Boolean getIsVatAdded();
        String getMushakIncluded();
        String getDeliveryCharge();
        BigDecimal getDeliveryChargeAmount();
        BigDecimal getVatPercent();
        BigDecimal getVatAmount();

        String getWarrantyUnit();
        Integer getWarrantyDuration();

        Long getWarehouseId();
        Long getCvdId();

    }
}
