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

    String pendingVerifications= """
            SELECT
            i.id as id,
            csheet.id as csId,
            CASE WHEN csheet.cs_status != 'REVIEW' AND (cvah.id IS NOT NULL AND cvah.employee_id = :nextVerifierId) THEN
                    cvah.cs_status
                ELSE
                    csheet.cs_status
            END  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
             CONCAT(c.name ,'-', sc.name) as categoryName,
             count(csd.id)                as items,
             COALESCE(sum(idd.rfq_qty),0) as rfqQty,
             COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs csheet
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            LEFT JOIN cs_verification_approval_histories cvah ON cvah.cs_id = csheet.id
            WHERE  (
                    (csheet.next_verifier_id = :nextVerifierId AND 
                    csheet.cs_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                    OR 
                    (cvah.employee_id = :nextVerifierId AND cvah.cs_status = 'VERIFIED')
                )
            
            GROUP BY csheet.id
            """;
    String countPendingVerifications="SELECT COUNT(*) FROM ("+pendingVerifications+") as total";

    String pendingApprovals= """
            SELECT
            i.id as id,
            csheet.id as csId,
            CASE WHEN csheet.cs_status != 'REVIEW' AND (cvah.id IS NOT NULL AND cvah.employee_id = :nextVerifierId) THEN
                    cvah.cs_status
                ELSE
                    csheet.cs_status
            END  as status,
            csheet.created_at    as csDate,
            csheet.cs_no                      as csNo,
            CONCAT(c.name ,'-', sc.name) as categoryName,
            count(csd.id)                as items,
            COALESCE(sum(idd.rfq_qty),0)   as rfqQty,
            COALESCE((SELECT count(*) FROM price_quotations pq 
                            WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
        FROM cs csheet
        LEFT JOIN indents i ON i.id = csheet.indent_id
        LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
        LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
        LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
        LEFT JOIN scm_item_categories c ON c.id = i.category_id
        LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
        LEFT JOIN cs_verification_approval_histories cvah ON cvah.cs_id = csheet.id
        WHERE  (
                (csheet.next_approver_id = :nextApproverId AND 
                csheet.cs_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                OR
                (cvah.employee_id = :nextVerifierId AND cvah.cs_status = 'APPROVED')
            )
            
            GROUP BY csheet.id
            """;

    String countPendingApprovals="SELECT COUNT(*) FROM ("+pendingApprovals+") as total";

    String closedCs="""
        SELECT
        i.id as id,
        csheet.id as csId,
        csheet.cs_status as status,
        csheet.created_at    as csDate,
        csheet.cs_no                      as csNo,
        CONCAT(c.name ,'-', sc.name) as categoryName,
        count(csd.id)                as items,
        COALESCE(sum(idd.rfq_qty),0)   as rfqQty,
        COALESCE((SELECT count(*) FROM price_quotations pq 
                        WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
    FROM cs csheet
    LEFT JOIN indents i ON i.id = csheet.indent_id
    LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
    LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
    LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
    LEFT JOIN scm_item_categories c ON c.id = i.category_id
    LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
    WHERE csheet.cs_status IN ('REJECTED', 'APPROVED','VERIFIED','COMPLETED')
    GROUP BY csheet.id
        """;
    String countClosedCs="SELECT COUNT(*) FROM ("+closedCs+") as total";

    String approvedCs= """
            SELECT
            i.id as id,
            csheet.id as csId,
            csheet.cs_status as status,
            csheet.created_at    as csDate,
            csheet.cs_no                      as csNo,
            CONCAT(c.name ,'-', sc.name) as categoryName,
            count(csd.id)                as items,
            COALESCE(sum(idd.rfq_qty),0)   as rfqQty,
            COALESCE((SELECT count(*) FROM price_quotations pq 
                            WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
        FROM cs csheet
        LEFT JOIN indents i ON i.id = csheet.indent_id
        LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
        LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
        LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
        LEFT JOIN scm_item_categories c ON c.id = i.category_id
        LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
        WHERE csheet.cs_status IN ('APPROVED')
        GROUP BY csheet.id
            """;

    String countApprovedCs="SELECT COUNT(*) FROM ("+approvedCs+") as total";
}
