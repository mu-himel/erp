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
                           pqd.warranty_unit as warrantyUnit,
                           csinfo.warehouse_id as warehouseId,
                           csinfo.cvdId as cvdId,
                           CASE WHEN pqd.extended_attributes IS NOT NULL THEN
                                  (select csinfo.order_qty-COALESCE (SUM(pod.delivery_qty),0) FROM purchase_orders po
                                  LEFT JOIN cs_po cpo ON cpo.id = po.po_group_id 
                                LEFT JOIN purchase_order_details pod ON pod.purchase_order_id  = po.id
                                WHERE po.vendor_id = :vendorId AND po.cs_id = csinfo.csId
                                 AND cpo.purchase_order_status NOT IN ('REJECTED')
                                 AND pod.item_name = CONCAT(pqd.brand_name,'-',pqd.item_attribute,'-',pqd.extended_attributes))
                                ELSE
                                (select csinfo.order_qty-COALESCE (SUM(pod.delivery_qty),0) FROM purchase_orders po
                                LEFT JOIN cs_po cpo ON cpo.id = po.po_group_id
                                LEFT JOIN purchase_order_details pod ON pod.purchase_order_id  = po.id
                                WHERE po.vendor_id = :vendorId  AND po.cs_id = csinfo.csId
                                 AND cpo.purchase_order_status NOT IN ('REJECTED')
                                 AND pod.item_name = CONCAT(pqd.brand_name,'-',pqd.item_attribute))
                                END as remainingQty
                           FROM price_quotation_details pqd
                           LEFT JOIN price_quotation_summary pqs ON pqs.price_quotation_id = pqd.price_quotation_id
                           LEFT JOIN (SELECT i.warehouse_id, cst.id as csId, cvd.id as cvdId, idd.sub_category_id, idd.brand_id,
                            scb.name as brand_name, idd.item_attribute, 
                           cvd.price_quotation_id, cvd.transaction_type,cvd.discount_amount, cvd.order_qty ,
                            cst.cs_no ,cvd.vendor_id  FROM cs_details csd
            LEFT JOIN cs cst ON cst.id=csd.cs_id
            LEFT JOIN cs_vendor_details cvd ON cvd.cs_detail_id = csd.id
            LEFT JOIN indent_details idd ON csd.indent_detail_id = idd.id
            LEFT JOIN scm_category_brands scb ON scb.id = idd.brand_id
            LEFT JOIN indents i ON i.id = idd.indent_id
            WHERE cst.cs_no IN (:csNo) AND cvd.vendor_id=:vendorId) csinfo ON csinfo.item_attribute=pqd.item_attribute 
            AND csinfo.brand_name = pqd.brand_name 
            WHERE pqd.price_quotation_id = csinfo.price_quotation_id
            GROUP BY pqd.brand_name, pqd.item_attribute ,csinfo.order_qty
              """, nativeQuery = true)
    List<CsVendorItemInfo> findAllItemsByVendor(String csNo, Long vendorId);

    @Query(value = """
            select sw.name as name,
            idd.warehouse_id as warehouseId,
            pr_qty as prQty,
            rfq_qty as rfqQty,
            concat(scb.name,' - ',id.item_attribute) as itemAttributeName
            FROM cs_vendor_details cvd
                        LEFT JOIN cs_details cd ON cvd.cs_detail_id =cd.id
                        LEFT JOIN cs ON cs.id = cd.cs_id
                        LEFT JOIN indent_details id ON id.id = cd.indent_detail_id\s
                        LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = id.id
                        LEFT JOIN scm_warehouses sw ON sw.id = idd.warehouse_id
                        LEFT JOIN scm_category_brands scb ON scb.id = id.brand_id\s
            			where cvd.vendor_id = :vendorId AND cs.cs_no = :csNo
            """,nativeQuery = true)
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
