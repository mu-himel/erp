package com.agi.aesl.erpscm.purchase_order.repository;

public interface PoQuery {
    String getPendingPOs= """
            SELECT * FROM (
            SELECT 
            po.po_group_id as id,
            cpo.po_date as poDate,
            GROUP_CONCAT(DISTINCT po.id) as poIds,
            i.indent_no                      as indentNo,
            po.po_no as poNo,
            (SELECT MAX(vendor_name) FROM price_quotations pq WHERE pq.vendor_id = po.vendor_id) vendorName,
            CONCAT(c.name,'-',sc.name)       as categoryName,
            COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
            COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
            cpo.purchase_order_status as status
            FROM purchase_orders po
            LEFT JOIN  cs_po cpo  ON cpo.id = po.po_group_id
            LEFT JOIN  cs csheet ON csheet.id = cpo.cs_id
            LEFT JOIN indents i ON csheet.indent_id = i.id
            LEFT JOIN scm_item_categories c ON i.category_id = c.id
            LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
            WHERE (:csNo IS NULL OR csheet.cs_no LIKE CONCAT('%',:csNo,'%'))
            AND (:poNo IS NULL OR po.po_no LIKE CONCAT('%',:poNo,'%')) 
            AND (:categoryId IS NULL OR i.category_id = :categoryId)
            AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
            AND (COALESCE(:fromDate) IS NULL OR cpo.po_date BETWEEN :fromDate AND :toDate)
            AND cpo.purchase_order_status  IN (:status)
            AND csheet.cs_status IN ('APPROVED','VERIFIED','COMPLETED')
            GROUP BY po.id) r 
            WHERE (:vendor IS NULL OR LOWER(r.vendorName) LIKE CONCAT('%',LOWER(:vendor),'%'))
            """;

    String countPendingPOs="SELECT COUNT(*) FROM ("+ getPendingPOs + ") as total";

    String getPendingVerificationPOs = """
            SELECT * FROM (SELECT
                   cpo.id as id,
                   cpo.po_date as poDate,
                   (SELECT MAX(vendor_name) FROM price_quotations pq WHERE pq.vendor_id = po.vendor_id) vendorName,
                   GROUP_CONCAT(DISTINCT po.id) as poIds,
                   i.indent_no                      as indentNo,
                   po.po_no as poNo,
                   CONCAT(c.name,'-',sc.name)       as categoryName,
                   COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
                   COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
                   CASE WHEN cpo.purchase_order_status != 'REVIEW' AND (pvah.id IS NOT NULL AND pvah.po_id = cpo.id 
                   AND pvah.employee_id = :userId AND pvah.po_status IN ('VERIFIED')) THEN
                                       pvah.po_status
                                   ELSE
                                       cpo.purchase_order_status
                              END as status
               FROM purchase_orders po
               LEFT JOIN cs_po cpo ON cpo.id = po.po_group_id
               LEFT JOIN cs csheet ON csheet.id = cpo.cs_id
               LEFT JOIN indents i ON csheet.indent_id = i.id
               LEFT JOIN scm_item_categories c ON i.category_id = c.id
               LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
               LEFT JOIN po_verification_approval_histories pvah ON pvah.po_id = po.id
               WHERE (:csNo IS NULL OR i.indent_no LIKE CONCAT('%',:csNo,'%'))
                AND (:poNo IS NULL OR po.po_no LIKE CONCAT('%',:poNo,'%')) 
                AND (:categoryId IS NULL OR i.category_id = :categoryId)
                AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
                AND (COALESCE(:fromDate) IS NULL OR cpo.po_date BETWEEN :fromDate AND :toDate)
                AND ((cpo.next_verifier_id = :userId AND cpo.purchase_order_status IN (:status))
               OR (pvah.employee_id=:userId AND pvah.po_status = 'VERIFIED'))
               AND csheet.cs_status IN ('VERIFIED','APPROVED','COMPLETED')
               GROUP BY cpo.id) r 
               WHERE (:vendor IS NULL OR LOWER(r.vendorName) LIKE CONCAT('%',LOWER(:vendor),'%'))
                    """;

    String countGetPendingVerificationPOs = "SELECT COUNT(*) FROM ("+getPendingVerificationPOs+") as total";

    String getPendingApprovalPOs= """
            SELECT * FROM (SELECT
                   cpo.id as id,
                   cpo.po_date as poDate,
                   (SELECT MAX(vendor_name) FROM price_quotations pq WHERE pq.vendor_id = po.vendor_id) vendorName,
                   GROUP_CONCAT(DISTINCT po.id) as poIds,
                   i.indent_no                      as indentNo,
                   po.po_no as poNo,
                   CONCAT(c.name,'-',sc.name)       as categoryName,
                   COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
                   COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
                   CASE WHEN cpo.purchase_order_status != 'REVIEW' AND (pvah.id IS NOT NULL AND pvah.po_id = cpo.id 
                   AND pvah.employee_id = :userId AND pvah.po_status IN ('APPROVED')) THEN
                                       pvah.po_status
                                   ELSE
                                       cpo.purchase_order_status
                              END as status
               FROM purchase_orders po
               LEFT JOIN cs_po cpo ON cpo.id = po.po_group_id
               LEFT JOIN cs csheet ON csheet.id = cpo.cs_id
               LEFT JOIN indents i ON csheet.indent_id = i.id
               LEFT JOIN scm_item_categories c ON i.category_id = c.id
               LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
               LEFT JOIN po_verification_approval_histories pvah ON pvah.po_id = po.id
               WHERE (:csNo IS NULL OR i.indent_no LIKE CONCAT('%',:csNo,'%'))
                AND (:poNo IS NULL OR po.po_no LIKE CONCAT('%',:poNo,'%')) 
                AND (:categoryId IS NULL OR i.category_id = :categoryId)
                AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
                AND (COALESCE(:fromDate) IS NULL OR cpo.po_date BETWEEN :fromDate AND :toDate)
                AND ((cpo.next_approver_id = :userId AND cpo.purchase_order_status IN (:status))
               OR (pvah.employee_id=:userId AND pvah.po_status = 'APPROVED'))
               AND csheet.cs_status IN ('VERIFIED','APPROVED','COMPLETED')
               GROUP BY cpo.id) r
               WHERE (:vendor IS NULL OR LOWER(r.vendorName) LIKE CONCAT('%',LOWER(:vendor),'%'))
            """;

    String countGetPendingApprovalPOs="SELECT COUNT(*) FROM ("+getPendingApprovalPOs+") as total";

    String getClosedPOs= """
         SELECT * FROM (SELECT
                   cpo.id as id,
                   cpo.po_date as poDate,
                   (SELECT MAX(vendor_name) FROM price_quotations pq WHERE pq.vendor_id = po.vendor_id) vendorName,
                   GROUP_CONCAT(DISTINCT po.id) as poIds,
                   i.indent_no                      as indentNo,
                   po.po_no as poNo,
                   CONCAT(c.name,'-',sc.name)       as categoryName,
                   COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
                   COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
                   cpo.purchase_order_status as status
               FROM purchase_orders po
               LEFT JOIN cs_po cpo ON cpo.id = po.po_group_id
               LEFT JOIN cs csheet ON csheet.id = cpo.cs_id
               LEFT JOIN indents i ON csheet.indent_id = i.id
               LEFT JOIN scm_item_categories c ON i.category_id = c.id
               LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
               LEFT JOIN po_verification_approval_histories pvah ON pvah.po_id = po.id
               WHERE (:csNo IS NULL OR i.indent_no LIKE CONCAT('%',:csNo,'%'))
                AND (:poNo IS NULL OR po.po_no LIKE CONCAT('%',:poNo,'%')) 
                AND (:categoryId IS NULL OR i.category_id = :categoryId)
                AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
                AND (COALESCE(:fromDate) IS NULL OR cpo.po_date BETWEEN :fromDate AND :toDate)
                AND cpo.purchase_order_status IN (:status)
               AND csheet.cs_status IN ('VERIFIED','APPROVED','COMPLETED')
               GROUP BY cpo.id) r
               WHERE (:vendor IS NULL OR LOWER(r.vendorName) LIKE CONCAT('%',LOWER(:vendor),'%'))
            """;

    String countClosedPOs="SELECT COUNT(*) FROM ("+getClosedPOs+") as total";

    String getApprovedPOs= """
        SELECT * FROM (SELECT 
        po.id as id,
        po.po_date                       as poDate,
        i.indent_no                      as indentNo,
        CONCAT(c.name,'-',sc.name)       as categoryName,
        COUNT(ide.id)                    as itemQty,
        COALESCE(COUNT(pod.id), 0)       as totalOrderQty,
        pod.delivery_date  as deliveryDate,
        DATEDIFF(pod.delivery_date , CURRENT_DATE) as remainTime,
        po.status as status,
        pq.vendor_name as vendorName
        FROM purchase_orders po
        LEFT JOIN purchase_order_details pod ON pod.purchase_order_id = po.id
        LEFT JOIN cs_vendor_details csd ON csd.id=pod.cs_vendor_detail_id
        LEFT JOIN price_quotations pq ON pq.id = csd.price_quotation_id
        LEFT JOIN cs csheet ON po.cs_id = csheet.id
        LEFT JOIN indents i ON csheet.indent_id = i.id
        LEFT JOIN indent_details ide ON  ide.indent_id = i.id
        LEFT JOIN scm_item_categories c ON i.category_id = c.id
        LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
        WHERE  (:csNo IS NULL OR i.indent_no LIKE CONCAT('%',:csNo,'%'))
                AND (:poNo IS NULL OR po.po_no LIKE CONCAT('%',:poNo,'%')) 
                AND (:categoryId IS NULL OR i.category_id = :categoryId)
                AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
                AND (COALESCE(:fromDate) IS NULL OR po.po_date BETWEEN :fromDate AND :toDate)
                AND po.status IN (:status)
        GROUP BY po.id) r
        WHERE (:vendor IS NULL OR LOWER(r.vendorName) LIKE CONCAT('%', LOWER(:vendor), '%'))
            """;

    String countApprovedPOs="SELECT COUNT(*) FROM ("+ getApprovedPOs+") as total";

    String purchaseOrderDetail= """
                SELECT 
                po1.id as poId,
                cvd.transaction_type as transactionType, 
                pod.total_price as totalPrice, 
                cvd.order_qty as orderQty,
                cvdd.delivery_qty as deliveryOrderQty,
                cvdd.delivery_date as deliveryDate,
                cvdd.warehouse_id as warehouseId,
                cvd.price_quotation_id as priceQuotationId,
                cvd.vat_amount as vendorPartialVatAmount,
                pqs2.is_ait_added as isAitAdded,
                pqs2.is_vat_added as isVatAdded,
                pod.id as podId,
                pod.delivery_qty as deliveryQty,
                pod.item_name as itemName,
                pod.remaining_qty as remainingQty,
                pod.unit_price as unitPrice,
                pod.delivery_charge as deliveryCharge,
                pod.vat_amount as vatAmount,
                pod.sub_total as subTotal,
                (select CONCAT(vendor_name,',',vendor_id,',',COALESCE(credit_payment_duration,''),',',pqd.unit_price,',',pqd.est_delivery_days,',',pq.remote_offer_id,
                ',',pqs.delivery_charge,',',pqs.delivery_charge_amount,',',pqs.vat_percent,',',pqs.vat_amount,',',
                    pqd.item_attribute,',',
                    pqd.brand_name,',',COALESCE(pqd.extended_attributes,''),',',COALESCE(pqd.warranty_duration,''),',',COALESCE(pqd.warranty_unit,''),',',pq.vendor_type,
                    ',',pq.id,',',pq.vendor_email,',',pq.vendor_phone_no)  
                    FROM price_quotations pq
                LEFT JOIN price_quotation_details pqd ON pqd.price_quotation_id = pq.id
                LEFT JOIN price_quotation_summary pqs on pqs.price_quotation_id = pq.id
                WHERE pq.id=cvd.price_quotation_id AND pqd.brand_name = scb.name  AND pqd.item_attribute LIKE CONCAT('%',ide.item_attribute,'%')
                ) as summary
            from purchase_order_details pod
            LEFT JOIN purchase_orders po1 ON po1.id =  pod.purchase_order_id 
            left join cs_vendor_details cvd ON pod.cs_vendor_detail_id = cvd.id
            left join cs_vendor_delivery_details cvdd ON cvdd.vendor_delivery_detail_id  = cvd.id
            left join cs_details cd  ON cd.id = cvd.cs_detail_id
            left join price_quotation_summary pqs2 ON pqs2.price_quotation_id = cvd.price_quotation_id
            left join indent_details ide ON cd.indent_detail_id = ide.id
            LEFT JOIN scm_category_brands scb  ON scb.id = ide.brand_id
            where po1.id= :poId
                """;
}
