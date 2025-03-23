package com.agi.aesl.erpscm.cs.repository;

public interface AcsQuery {

    String getAllAcsByIndentNoAndStatus= """
            SELECT
            acs.id as acsId,
            csheet.validity_date as validityDate,
            i.id as id,
            csheet.id as csId,
            acs.acs_status  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
             CONCAT(c.name ,'-', sc.name) as categoryName,
             count(csd.id)                as items,
             COALESCE(sum(idd.rfq_qty),0) as rfqQty,
             COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs_accounts acs 
            LEFT JOIN cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo,'%'))
            AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status))
            AND (COALESCE(:fromDate) IS NULL OR acs.created_at BETWEEN :fromDate AND :toDate)
            GROUP BY acs.id
            """;

    String countByIndentNoAndStatusAcs="SELECT COUNT(*) as total FROM ("+getAllAcsByIndentNoAndStatus+") as t";

    String getAllPVAcsByIndentNoAndStatus = """
            SELECT * FROM (SELECT
            acs.id as acsId,
            csheet.validity_date as validityDate,
            i.id as id,
            csheet.id as csId,
            CASE WHEN acs.acs_status != 'REVIEW' AND (cavah.id IS NOT NULL AND cavah.cs_account_id = acs.id 
            AND cavah.employee_id = :nextVerifierId AND cavah.acs_status IN ('VERIFIED')) THEN
                    cavah.acs_status
                ELSE
                    acs.acs_status
            END  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
             CONCAT(c.name ,'-', sc.name) as categoryName,
             count(csd.id)                as items,
             COALESCE(sum(idd.rfq_qty),0) as rfqQty,
             COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs_accounts acs 
            LEFT JOIN cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            LEFT JOIN cs_account_va_histories cavah ON cavah.cs_account_id = acs.id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo))
            AND
            (
                    (acs.next_verifier_id = :nextVerifierId AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status)))
                    OR 
                    (cavah.employee_id = :nextVerifierId AND  cavah.acs_status IN ('REVIEW','VERIFIED'))
            )
            AND (COALESCE(:fromDate) IS NULL OR acs.created_at BETWEEN :fromDate AND :toDate)
            GROUP BY acs.id) p WHERE (:searchStatus IS NULL OR p.status = :searchStatus)
            """;

    String countByPVAcsIndentNoAndStatusAcs="SELECT COUNT(*) FROM ("+ getAllPVAcsByIndentNoAndStatus +") as t";

    String getAllPAAcsByIndentNoAndStatus= """
            SELECT
            acs.id as acsId,
            csheet.validity_date as validityDate,
            i.id as id,
            csheet.id as csId,
            CASE WHEN acs.acs_status != 'REVIEW' AND (cavah.id IS NOT NULL AND cavah.cs_account_id = acs.id 
            AND cavah.employee_id = :nextApproverId
            AND cavah.acs_status IN ('APPROVED')) THEN
                    cavah.acs_status
                ELSE
                    acs.acs_status
            END  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
             CONCAT(c.name ,'-', sc.name) as categoryName,
             count(csd.id)                as items,
             COALESCE(sum(idd.rfq_qty),0) as rfqQty,
             COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs_accounts acs 
            LEFT JOIN cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            LEFT JOIN cs_account_va_histories cavah ON cavah.cs_account_id = acs.id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo))
            AND
            (
                    (acs.next_approver_id = :nextApproverId AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status)))
                    OR 
                    (cavah.employee_id = :nextApproverId AND cavah.acs_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
            )
            AND (COALESCE(:fromDate) IS NULL OR acs.created_at BETWEEN :fromDate AND :toDate)
            GROUP BY acs.id
            """;

    String countByPAAcsIndentNoAndStatusAcs="SELECT COUNT(*) FROM ("+ getAllPAAcsByIndentNoAndStatus +") as t";

    String getAllActiveCsByIndentNo= """
            SELECT
            acs.id as acsId,
            csheet.validity_date as validityDate,
            i.id as id,
            csheet.id as csId,
            acs.acs_status  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
            CONCAT(c.name ,'-', sc.name) as categoryName,
            count(csd.id)                as items,
            COALESCE(sum(idd.rfq_qty),0) as rfqQty,
            COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs_accounts acs 
            LEFT JOIN cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo))
            AND (:categoryId IS NULL OR i.category_id = :categoryId)
            AND (:subCategoryId IS NULL OR i.sub_category_id = :subCategoryId)
            AND (COALESCE(:fromDate) IS NULL OR (csheet.validity_date BETWEEN :fromDate AND :toDate)) 
            AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status))
            AND (COALESCE(:status) IS NULL OR csheet.cs_status IN (:status)) AND CAST(CONCAT(csheet.validity_date,' 23:59:59') as datetime) >= SYSDATE()
            GROUP BY acs.id
            """;

    String countAllActiveCsByIndentNo="SELECT COUNT(*) FROM ("+getAllActiveCsByIndentNo+") as t";

    String getAllExpiredCsByIndentNo= """
            SELECT
            acs.id as acsId,
            csheet.validity_date as validityDate,
            i.id as id,
            csheet.id as csId,
            acs.acs_status  as status,
            csheet.created_at               as csDate,
            csheet.cs_no                     as csNo,
            CONCAT(c.name ,'-', sc.name) as categoryName,
            count(csd.id)                as items,
            COALESCE(sum(idd.rfq_qty),0) as rfqQty,
            COALESCE((SELECT count(*) FROM price_quotations pq 
                                WHERE status='LOCKED' AND rfq_id = csheet.indent_id),0) as lockedVendor
            FROM cs_accounts acs 
            LEFT JOIN cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo))
            AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status))
            AND csheet.validity_date > SYSDATE()
            GROUP BY acs.id
            """;

    String countAllExpiredCsByIndentNo = "SELECT COUNT(*) FROM ("+getAllExpiredCsByIndentNo+") as t";
}
