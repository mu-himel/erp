package com.agi.aesl.erpscm.cs.repository;

public class CsQuery {

    private CsQuery(){}
    public static final String COUNT_START="SELECT COUNT(*) FROM (";
    public static final String COUNT_END=") as TOTAL";
    public static final String FETCH_LOCKED_VENDORS_BY_ITEM="""
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
    AND pqd.item_attribute = :itemName 
    AND (:brandName IS NULL OR pqd.brand_name = :brandName)
    AND pq.is_recommend_for_cs = 1 AND pq.status = 'LOCKED'
    group by vendor_id,brandName,pqd.item_attribute
            """;

    public static final String FETCH_LOCKED_VENDORS_BY_VENDOR_AND_ITEM= """
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

    public static final String PENDING_VERIFICATIONS= """
            SELECT * FROM (SELECT
            i.id as id,
            csheet.id as csId,
            CASE WHEN csheet.cs_status != 'REVIEW' AND (cvah.id IS NOT NULL AND cvah.cs_id = csheet.id 
            AND cvah.employee_id = :nextVerifierId AND cvah.cs_status IN ('VERIFIED')) THEN
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
            WHERE  (:indentNo IS NULL OR i.indent_no LIKE CONCAT('%',:indentNo,'%'))
            AND (
                    (csheet.next_verifier_id = :nextVerifierId AND 
                    csheet.cs_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                    OR 
                    (cvah.employee_id = :nextVerifierId AND cvah.cs_status = 'VERIFIED')
                )
            GROUP BY csheet.id) r WHERE r.status IN (:statuses)
            AND (:fromDate IS NULL OR r.csDate BETWEEN :fromDate AND :toDate)
            """;
    public static final String COUNT_PENDING_VERIFICATIONS=COUNT_START+PENDING_VERIFICATIONS+COUNT_END;

    public static final String PENDING_APPROVALS= """
            SELECT * FROM (SELECT
            i.id as id,
            csheet.id as csId,
            CASE WHEN csheet.cs_status != 'REVIEW' AND (cvah.id IS NOT NULL AND cvah.cs_id = csheet.id 
            AND cvah.employee_id = :nextApproverId AND cvah.cs_status IN ('APPROVED')) THEN
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
        WHERE   (:indentNo IS NULL OR i.indent_no LIKE CONCAT('%',:indentNo,'%'))
        AND (
                (csheet.next_approver_id = :nextApproverId AND 
                csheet.cs_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                OR
                (cvah.employee_id = :nextApproverId AND cvah.cs_status = 'APPROVED')
            )
            GROUP BY csheet.id) r WHERE r.status IN (:statuses)
            AND (:fromDate IS NULL OR r.csDate BETWEEN :fromDate AND :toDate)
            """;

    public static final String COUNT_PENDING_APPROVALS=COUNT_START+PENDING_APPROVALS+COUNT_END;

    public static final String CLOSED_CS="""
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
    WHERE (:indentNo IS NULL OR i.indent_no LIKE CONCAT('%',:indentNo,'%')) 
    AND (COALESCE(:statuses) IS NULL OR csheet.cs_status IN (:statuses))
    AND (COALESCE(:fromDate) IS NULL OR csheet.created_at BETWEEN :fromDate AND :toDate)
    GROUP BY csheet.id
        """;
    public static final String COUNT_CLOSED_CS=COUNT_START+CLOSED_CS+COUNT_END;

    public static final String APPROVED_CS= """
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
        WHERE 
        (:indentNo IS NULL OR i.indent_no LIKE CONCAT('%',:indentNo,'%'))
        AND (COALESCE(:statuses) IS NULL OR csheet.cs_status IN (:statuses))
        AND (COALESCE(:fromDate) IS NULL OR csheet.created_at BETWEEN :fromDate AND :toDate)
        GROUP BY csheet.id
            """;

    public static final String COUNT_APPROVED_CS=COUNT_START+APPROVED_CS+COUNT_END;

    public static final String GET_POTENTIAL_PO_LIST_FROM_CS= """
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
                """;

    public static final String GET_ALL_VENDOR_WAREHOUSE= """
                SELECT sw.name as name,idd.warehouse_id as warehouseId,pr_qty as prQty,
                        rfq_qty as rfqQty,
                        CASE WHEN scb.id IS NULL THEN
                            id.item_attribute
                        ELSE
                            concat(scb.name,' - ',id.item_attribute)
                        END as itemAttributeName
                FROM cs_vendor_details cvd
                LEFT JOIN cs_details cd ON cvd.cs_detail_id =cd.id
                LEFT JOIN cs ON cs.id = cd.cs_id
                LEFT JOIN indent_details id ON id.id = cd.indent_detail_id
                LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = id.id
                LEFT JOIN scm_warehouses sw ON sw.id = idd.warehouse_id
                LEFT JOIN scm_category_brands scb ON scb.id = id.brand_id
                where cvd.vendor_id = :vendorId AND cs.cs_no = :csNo
            """;

    public static final String GET_ALL_ITEMS_BY_VENDOR="""
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
            AND csinfo.brand_name IS NULL OR csinfo.brand_name = pqd.brand_name
            WHERE pqd.price_quotation_id = csinfo.price_quotation_id
            GROUP BY pqd.brand_name, pqd.item_attribute ,csinfo.order_qty
            """;
}
