package com.agi.aesl.erpscm.indent.repository;

public interface IndentQuery {

    String getAllIndents =
            """
                    SELECT i.id                                    as id,
                           i.indent_no                             as indentNo,
                           i.category_id                           as categoryId,
                           c.name                                  as categoryName,
                           COUNT(ide.id)                           as itemsCount,
                           COALESCE(SUM(idd.order_qty), 0)         as orderQty,
                           i.priority                              as priority,
                           i.status                                as status
                     
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN item_categories c on i.category_id = c.id
                             LEFT JOIN item_categories sc on ide.sub_category_id = sc.id
                     
                    WHERE 
                    (COALESCE(:warehouseIds) IS NULL OR i.warehouse_id IN (:warehouseIds))
                    AND 
                    (
                        (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
                        OR 
                        (COALESCE(:categoryIds) IS NULL OR sc.id IN (:categoryIds))
                    )
                     
                    GROUP BY i.id
                                                                                """;

    String countAllIndents = "SELECT COUNT(*) FROM ("+getAllIndents+") as total";

    String getIndentPendingVerifications = """
            SELECT i.id                                    as id,
                           i.indent_no                             as indentNo,
                           i.indent_date                           as indentDate,
                           i.category_id                           as categoryId,
                           c.name                                  as categoryName,
                           GROUP_CONCAT(DISTINCT sc.name)          as subCategoryName,
                           COUNT(ide.id)                           as itemsCount,
                           COALESCE(SUM(idd.order_qty), 0)         as orderQty,
                           i.priority                              as priority,
                           i.status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName,
                           (SELECT indent_status FROM indent_verification_approval_histories
                                   where employee_id = :nextVerifierId AND indent_id=i.id AND indent_status='VERIFIED') as indentStatus
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN indent_verification_approval_histories ivah ON ivah.indent_id = i.id
                             LEFT JOIN item_categories c on i.category_id = c.id
                             LEFT JOIN item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                     
                    WHERE  ((i.next_verifier_id = :nextVerifierId AND i.status IN ('PENDING_VERIFICATION', 'REVIEW','VERIFIED'))
                        OR (ivah.employee_id = :nextVerifierId AND ivah.indent_status = 'VERIFIED'))
                        AND (COALESCE(:warehouseIds) IS NULL OR i.warehouse_id IN (:warehouseIds))
                        AND 
                        (
                            (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
                            OR 
                            (COALESCE(:categoryIds) IS NULL OR sc.id IN (:categoryIds))
                        )
                     
                    GROUP BY i.id
            """;

    String countAllPendingVerifications="SELECT COUNT(*) FROM ("+getIndentPendingVerifications+") as total";

    String getIndentPendingApprovals = """
                    SELECT i.id                                    as id,
                           i.indent_no                             as indentNo,
                           i.indent_date                           as indentDate,
                           i.category_id                           as categoryId,
                           c.name                                  as categoryName,
                           GROUP_CONCAT(DISTINCT sc.name)          as subCategoryName,
                           COUNT(ide.id)                           as itemsCount,
                           COALESCE(SUM(idd.order_qty), 0)         as orderQty,
                           i.priority                              as priority,
                           i.status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName,
                           (SELECT indent_status FROM indent_verification_approval_histories
                                   where employee_id = :nextApproverId AND indent_id=i.id AND indent_status='APPROVED') as indentStatus
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN indent_verification_approval_histories ivah ON ivah.indent_id = i.id
                             LEFT JOIN item_categories c on i.category_id = c.id
                             LEFT JOIN item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                     
                    WHERE  ((i.next_approver_id = :nextApproverId AND i.status IN ('PENDING_APPROVAL', 'REVIEW','APPROVED'))
                        OR (ivah.employee_id = :nextApproverId AND ivah.indent_status = 'APPROVED'))
                        AND (COALESCE(:warehouseIds) IS NULL OR i.warehouse_id IN (:warehouseIds))
                        AND 
                        (
                            (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
                            OR 
                            (COALESCE(:categoryIds) IS NULL OR sc.id IN (:categoryIds))
                        )
                     
                    GROUP BY i.id
            """;

    String countAllPendingApprovals="SELECT COUNT(*) FROM ("+getIndentPendingApprovals+") as total";

    String getClosedIndents = """
            SELECT i.id                                    as id,
                           i.indent_no                             as indentNo,
                           i.category_id                           as categoryId,
                           c.name                                  as categoryName,
                           sc.name                                 as subCategoryName,
                           COUNT(ide.id)                           as itemsCount,
                           COALESCE(SUM(idd.order_qty), 0)         as orderQty,
                           i.priority                              as priority,
                           i.status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN item_categories c on i.category_id = c.id
                             LEFT JOIN item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                    WHERE i.status IN ('COMPLETED','REJECTED') 
                        AND (
                            (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
                            OR 
                            (COALESCE(:categoryIds) IS NULL OR sc.id IN (:categoryIds))
                        )
                      AND (COALESCE(:warehouseIds) IS NULL OR i.warehouse_id IN (:warehouseIds))
                     
                    GROUP BY i.id
            """;

    String countAllClosed="SELECT COUNT(*) FROM ("+getClosedIndents+") as total";
}
