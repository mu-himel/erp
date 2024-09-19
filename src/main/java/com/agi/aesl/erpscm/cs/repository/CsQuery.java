package com.agi.aesl.erpscm.cs.repository;

public interface CsQuery {
    String fetchLockedVendorsByItem="""
        SELECT 
            pqd.rfq_qty as rfqQty, 
            pqd.total_price as totalPrice, 
            pqd.unit_price as unitPrice, 
            pqd.brand_name as brandName,
            pqd.extended_attributes as extendedAttributes,
            pqd.item_attribute as itemAttribute,
            pqd.est_delivery_days as estDeliveryDays, 
            pqd.warranty_duration as warrantyDuration,
            pqd.warranty_unit as warrantyUnit,
            pq.id as pqId, 
            pq.vendor_id as vendorId, pq.vendor_name as vendorName,
            pq.vendor_email as vendorEmail, pq.vendor_phone_no as vendorPhoneNo,
            pq.vendor_type as vendorType,
            pq.score as score,
            pq.rfq_id as rfqId, pq.status as status, pq.price_quotation_status as priceQuotationStatus,
            pqs.credit_payment_unit as creditPaymentUnit, 
            pqs.credit_payment_duration as creditPaymentDuration, pqs.delivery_charge as deliveryCharge,
            pqs.delivery_charge_amount as deliveryChargeAmount,
            pqs.is_vat_added as isVatAdded, pqs.is_ait_added as isAitAdded,
            pqs.vat_percent as vatPercent, pqs.vat_amount as vatAmount, pqs.sub_total_price as subTotalPrice, 
            pqs.total_price as totalPriceInSummary,
            pq.payment_method as paymentMethod
    FROM price_quotations pq
    LEFT JOIN price_quotation_details pqd ON pqd.price_quotation_id = pq.id
    LEFT JOIN price_quotation_summary pqs ON pqs.price_quotation_id = pq.id
    WHERE pq.id IN (select id FROM price_quotations pq2 where pq2.rfq_id=:tenderId) 
    AND pqd.item_attribute = :itemName AND pq.is_recommend_for_cs = 1 AND pq.status = 'LOCKED'
    group by vendor_id
            """;

    String fetchLockedVendorsByVendorAndItem= """
            SELECT 
                    pqd.rfq_qty as rfqQty, 
                    pqd.total_price as totalPrice, 
                    pqd.unit_price as unitPrice, 
                    pqd.est_delivery_days as estDeliveryDays, 
                    pqd.warranty_duration as warrantyDuration,
                    pqd.warranty_unit as warrantyUnit,
                    pqd.extended_attributes as extendedAttributes,
                    pqd.item_attribute as itemAttribute,
                    pqd.brand_name as brandName,
                    pq.id as pqId, 
                    pq.vendor_id as vendorId, pq.vendor_name as vendorName,
                    pq.vendor_email as vendorEmail, pq.vendor_phone_no as vendorPhoneNo,
                    pq.vendor_type as vendorType,
                    pq.score as score,
                    pq.rfq_id as rfqId, pq.status as status, pq.price_quotation_status as priceQuotationStatus,
                    pqs.credit_payment_unit as creditPaymentUnit, 
                    pqs.credit_payment_duration as creditPaymentDuration, pqs.delivery_charge as deliveryCharge,
                    pqs.delivery_charge_amount as deliveryChargeAmount,
                    pqs.is_vat_added as isVatAdded, pqs.is_ait_added as isAitAdded,
                    pqs.vat_percent as vatPercent, pqs.vat_amount as vatAmount, pqs.sub_total_price as subTotalPrice, 
                    pqs.total_price as totalPriceInSummary,
                    pq.payment_method as paymentMethod
            FROM price_quotations pq
            LEFT JOIN price_quotation_details pqd ON pqd.price_quotation_id = pq.id
            LEFT JOIN price_quotation_summary pqs ON pqs.price_quotation_id = pq.id
            WHERE pq.id IN (select max(id) FROM price_quotations pq2 where pq2.rfq_id=:tenderId AND pq2.vendor_id = :vendorId AND pq2.status='LOCKED') 
            AND (:itemName IS NULL OR pqd.item_attribute = :itemName)
            AND pq.vendor_id = :vendorId AND pq.is_recommend_for_cs = 1 AND pq.status = 'LOCKED'
            group by vendor_id
            """;
}
