package com.agi.aesl.erpscm.purchase_order.repository;

public interface PoQuery {
    String getPendingPOs= """
            SELECT 
            csheet.id as id,
            (select cvah.verification_date 
                FROM cs_verification_approval_histories cvah 
                WHERE cvah.cs_id=csheet.id 
                AND cvah.cs_status IN ('APPROVED','VERIFIED')
            ) as poDate,
            GROUP_CONCAT(DISTINCT po.id) as poIds,
            i.indent_no                      as indentNo,
            CONCAT(c.name,'-',sc.name)       as categoryName,
            COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
            COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
            cpo.purchase_order_status as status
        FROM cs csheet
        LEFT JOIN cs_po cpo ON cpo.cs_id = csheet.id
        LEFT JOIN purchase_orders po ON po.cs_id = csheet.id
        LEFT JOIN indents i ON csheet.indent_id = i.id
        LEFT JOIN scm_item_categories c ON i.category_id = c.id
        LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
        WHERE cpo.purchase_order_status IN ('PENDING','PENDING_VERIFICATION','PENDING_APPROVAL','REVIEW')
        AND csheet.cs_status IN ('APPROVED','VERIFIED','COMPLETED')
        GROUP BY csheet.id
            """;

    String countPendingPOs="SELECT COUNT(*) FROM ("+ getPendingPOs + ") as total";

    String getPendingVerificationPOs = """
        SELECT 
        cpo.id as poGroupId,
        csheet.id as id,
        (select cvah.verification_date 
            FROM cs_verification_approval_histories cvah 
            WHERE cvah.cs_id=csheet.id 
            AND cvah.cs_status IN ('APPROVED','VERIFIED')
        ) as poDate,
        GROUP_CONCAT(DISTINCT po.id) as poIds,
        i.indent_no                      as indentNo,
        CONCAT(c.name,'-',sc.name)       as categoryName,
        COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
        COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
        cpo.purchase_order_status as status
    FROM cs csheet
    LEFT JOIN cs_po cpo ON cpo.cs_id = csheet.id
    LEFT JOIN purchase_orders po ON po.cs_id = csheet.id
    LEFT JOIN indents i ON csheet.indent_id = i.id
    LEFT JOIN scm_item_categories c ON i.category_id = c.id
    LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
    WHERE cpo.purchase_order_status IN ('PENDING_VERIFICATION')
    AND csheet.cs_status IN ('APPROVED','VERIFIED','COMPLETED')
    GROUP BY cpo.id
            """;

    String countGetPendingVerificationPOs = "SELECT COUNT(*) FROM ("+getPendingVerificationPOs+") as total";

    String getPendingApprovalPOs= """
            SELECT 
        cpo.id as poGroupId,
        csheet.id as id,
        (select cvah.verification_date 
            FROM cs_verification_approval_histories cvah 
            WHERE cvah.cs_id=csheet.id 
            AND cvah.cs_status IN ('APPROVED','VERIFIED')
        ) as poDate,
        GROUP_CONCAT(DISTINCT po.id) as poIds,
        i.indent_no                      as indentNo,
        CONCAT(c.name,'-',sc.name)       as categoryName,
        COALESCE((select count(*) from cs_details cd where cd.cs_id = csheet.id),0)  as itemQty,
        COALESCE((select count(*) from purchase_orders po2 where po2.cs_id = csheet.id), 0) as totalOrderQty,
        cpo.purchase_order_status as status
    FROM cs csheet
    LEFT JOIN cs_po cpo ON cpo.cs_id = csheet.id
    LEFT JOIN purchase_orders po ON po.cs_id = csheet.id
    LEFT JOIN indents i ON csheet.indent_id = i.id
    LEFT JOIN scm_item_categories c ON i.category_id = c.id
    LEFT JOIN scm_item_categories sc ON i.sub_category_id = sc.id
    WHERE cpo.purchase_order_status IN ('PENDING_APPROVAL')
    AND csheet.cs_status IN ('APPROVED','VERIFIED','COMPLETED')
    GROUP BY cpo.id
            """;

    String countGetPendingApprovalPOs="SELECT COUNT(*) FROM ("+getPendingApprovalPOs+") as total";

    String getClosedPOs= """
            SELECT 
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
        WHERE  po.status IN ('REJECTED','COMPLETED')
        GROUP BY po.id
            """;

    String countClosedPOs="SELECT COUNT(*) FROM ("+getClosedPOs+") as total";

    String getApprovedPOs= """
            SELECT 
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
        WHERE  po.status IN ('APPROVED')
        GROUP BY po.id
            """;

    String countApprovedPOs="SELECT COUNT(*) FROM ("+ getApprovedPOs+") as total";
}
