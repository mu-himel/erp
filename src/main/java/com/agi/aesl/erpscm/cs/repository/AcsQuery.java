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
            FROM acs 
            LEFT cs csheet ON csheet.id = acs.cs_id
            LEFT JOIN indents i ON i.id = csheet.indent_id
            LEFT JOIN cs_details csd ON csd.cs_id = csheet.id
            LEFT JOIN indent_details ide ON ide.id = csd.indent_detail_id
            LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id  
            LEFT JOIN scm_item_categories c ON c.id = i.category_id
            LEFT JOIN scm_item_categories sc ON sc.id = i.sub_category_id
            WHERE (:indentNo IS NULL OR csheet.cs_no LIKE concat('%',:indentNo))
            AND (COALESCE(:status) IS NULL OR acs.acs_status IN (:status))
            GROUP BY acs.id
            """;

    String countByIndentNoAndStatusAcs="SELECT COUNT(*) as total FROM ("+getAllAcsByIndentNoAndStatus+")";
}
