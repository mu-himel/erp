package com.agi.aesl.erpscm.cs.repository;

import com.agi.aesl.erpscm.cs.entity.CsDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CsDetailRepository extends JpaRepository<CsDetail, Long> {

    @Query(value = """
              SELECT
                           pqd.brand_name as brandName,
                           pqd.extended_attributes as extendedAttributes,
                           pqd.item_attribute as itemAttribute,
                           pqd.est_delivery_days as estDeliveryDays,
                           pqd.unit_price as unitPrice,
                           csinfo.cs_no as csNo,
                           pqs.id as pqsId,
                           pqd.price_quotation_id as pqId,
                           pqd.id as pqdId,
                           csinfo.transaction_type as transactionType,
                           csinfo.discount_amount as discountAmount,
                           csinfo.order_qty as orderQty  ,
                           pqs.credit_payment_duration as duration,
                           pqs.credit_payment_unit as durationUnit,
                           pqs.is_ait_added as isAitAdded,
                           pqs.is_vat_added as isVatAdded,
                           pqs.mushak_included as mushakIncluded,
                           pqs.delivery_charge as deliveryCharge,
                           pqs.delivery_charge_amount as deliveryChargeAmount,
                           pqs.vat_percent as vatPercent,
                           pqs.vat_amount as vatAmount,
                           pqd.warranty_duration as warrantyDuration,
                           pqd.warranty_unit as warrantyUnit
                           FROM price_quotation_details pqd
                           LEFT JOIN price_quotation_summary pqs ON pqs.price_quotation_id = pqd.price_quotation_id
                           LEFT JOIN (SELECT idd.item_attribute, cvd.price_quotation_id, cvd.transaction_type,cvd.discount_amount, cvd.order_qty ,
                            cst.cs_no ,cvd.vendor_id  FROM cs_details csd
            LEFT JOIN cs cst ON cst.id=csd.cs_id
            LEFT JOIN cs_vendor_details cvd ON cvd.cs_detail_id = csd.id
            LEFT JOIN indent_details idd ON csd.indent_detail_id = idd.id
            WHERE cst.cs_no IN (:csNo) AND cvd.vendor_id=:vendorId) csinfo ON csinfo.item_attribute=pqd.item_attribute 
            WHERE pqd.price_quotation_id = csinfo.price_quotation_id
              """, nativeQuery = true)
    List<CsVendorItemInfo> findAllItemsByVendor(String csNo, Long vendorId);

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

    }
}
