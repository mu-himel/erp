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
                           i.priority_date_time                    as priority,
                           i.indent_status                                as status
                     
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                     
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
                           i.priority_date_time                              as priority,
                           i.indent_status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName,
                           (SELECT indent_status FROM indent_verification_approval_histories
                                   where employee_id = :nextVerifierId AND indent_id=i.id AND indent_status='VERIFIED') as indentStatus
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN indent_verification_approval_histories ivah ON ivah.indent_id = i.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                     
                    WHERE  ((i.next_verifier_id = :nextVerifierId AND i.indent_status IN ('PENDING_VERIFICATION', 'REVIEW','VERIFIED'))
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
                           i.priority_date_time                              as priority,
                           i.indent_status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName,
                           (SELECT indent_status FROM indent_verification_approval_histories
                                   where employee_id = :nextApproverId AND indent_id=i.id AND indent_status='APPROVED') as indentStatus
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN indent_verification_approval_histories ivah ON ivah.indent_id = i.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                     
                    WHERE  ((i.next_approver_id = :nextApproverId AND i.indent_status IN ('PENDING_APPROVAL', 'REVIEW','APPROVED'))
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
                           i.priority_date_time                              as priority,
                           i.indent_status                                as status,
                           CONCAT(e.employee_id,'-',e.employee_name)        as employeeName
                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN acl_users e ON e.id = i.requested_by_id
                    WHERE i.indent_status IN ('COMPLETED','REJECTED','APPROVED','VERIFIED') 
                        AND (
                            (COALESCE(:categoryIds) IS NULL OR c.id IN (:categoryIds))
                            OR 
                            (COALESCE(:categoryIds) IS NULL OR sc.id IN (:categoryIds))
                        )
                      AND (COALESCE(:warehouseIds) IS NULL OR i.warehouse_id IN (:warehouseIds))
                     
                    GROUP BY i.id
            """;

    String countAllClosed="SELECT COUNT(*) FROM ("+getClosedIndents+") as total";

    String getIndentDetail= """
            SELECT i.id                                              as id,
                           ide.id                                            as detailId,
                           idd.warehouse_id                                  as warehouseId,
                           w.name                                            as warehouseName,
                           idd.id                                            as piwId,
                           ipd.id                                            as pdId,
                           ipd.pd_date                                       as pdDate,
                           ipd.qty                                           as pdQty,
                           i.indent_no                                       as indentNo,
                           i.category_id                                     as categoryId,
                           c.name                                            as categoryName,
                           ide.sub_category_id                               as subCategoryId,
                           ide.product_requirements_ids                      as productRequirementIds,
                           sc.name                                           as subCategoryName,
                           ide.item_attribute                                as itemName,
                           COALESCE(SUM(idd.order_qty), 0)                   as orderQty,
                           idd.rfq_qty                                       as rfqQty,
                           COALESCE(SUM(idd.pr_qty), 0)                      as prQty,
                           i.priority                                        as priority,
                           i.priority_date_time                              as priorityDate,
                           DATEDIFF(i.priority_date_time,CURRENT_DATE)       as daysRemain,
                           ide.brand_id                                      as brandId,
                           (select name from scm_category_brands cb WHERE cb.id = ide.brand_id) as brandName

                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN scm_warehouses w ON w.id = idd.warehouse_id
                             LEFT JOIN indent_partial_deliveries ipd ON ipd.indent_delivery_detail_id = idd.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id


                    WHERE (:id IS NOT NULL AND i.id = :id)
                    group by ipd.id,idd.id
            """;

    String getIndentDetailWithIdRange= """
            SELECT i.id                                                      as id,
                           ide.id                                            as detailId,
                           idd.warehouse_id                                  as warehouseId,
                           w.name                                            as warehouseName,
                           idd.id                                            as piwId,
                           ipd.id                                            as pdId,
                           ipd.pd_date                                       as pdDate,
                           ipd.qty                                           as pdQty,
                           i.indent_no                                       as indentNo,
                           i.category_id                                     as categoryId,
                           c.name                                            as categoryName,
                           ide.sub_category_id                               as subCategoryId,
                           ide.product_requirements_ids                      as productRequirementIds,
                           sc.name                                           as subCategoryName,
                           ide.item_attribute                                as itemName,
                           COALESCE(SUM(idd.order_qty), 0)                   as orderQty,
                           idd.rfq_qty                                       as rfqQty,
                           COALESCE(SUM(idd.pr_qty), 0)                      as prQty,
                           i.priority                                        as priority,
                           i.priority_date_time                              as priorityDate,
                           DATEDIFF(i.priority_date_time,CURRENT_DATE)       as daysRemain,
                           ide.brand_id                                      as brandId,
                           (select name from scm_category_brands cb WHERE cb.id = ide.brand_id) as brandName

                    FROM indents i
                             LEFT JOIN indent_details ide on i.id = ide.indent_id
                             LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                             LEFT JOIN scm_warehouses w ON w.id = idd.warehouse_id
                             LEFT JOIN indent_partial_deliveries ipd ON ipd.indent_delivery_detail_id = idd.id
                             LEFT JOIN scm_item_categories c on i.category_id = c.id
                             LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                             LEFT JOIN scm_items it ON ide.item_id = it.id

                    WHERE i.istatus = 'INIT'
                    AND (COALESCE(:id) IS NOT NULL AND i.id IN (:ids))
                    group by i.category_id, i.sub_category_id,
                    CASE
                        WHEN it.id IS NOT NULL THEN it.id
                    END
            """;

    String getIndentApprovedAndPendingRFqWithSearch =
            """
                    SELECT i.id                                    as id,
                            i.indent_no                             as indentNo,
                            i.indent_date                           as indentDate,
                            i.category_id                           as categoryId,
                            c.name                                  as categoryName,
                            sc.name                                 as subCategoryName,
                            COUNT(ide.id)                           as itemsCount,
                            COALESCE(SUM(idd.order_qty), 0)         as orderQty,
                            i.priority_date_time                    as priority,
                            i.rfq_status                            as status,
                            CONCAT(e.employee_id,'-',e.employee_name)        as employeeName,
                            DATEDIFF(i.priority_date_time , CURRENT_DATE) as daysRemain
                            
                    FROM indents i
                                    LEFT JOIN indent_details ide on i.id = ide.indent_id
                                    LEFT JOIN indent_delivery_details idd ON idd.indent_detail_id = ide.id
                                    LEFT JOIN scm_item_categories c on i.category_id = c.id
                                    LEFT JOIN scm_item_categories sc on ide.sub_category_id = sc.id
                                    LEFT JOIN acl_users e ON e.id = i.requested_by_id
                            
                    WHERE  i.status = 'APPROVED' AND i.rfq_status = 'INIT'
                            AND (:indentNo IS NULL OR i.indent_no LIKE CONCAT('%',:indentNo))
                            AND (:category IS NULL OR  LOWER(c.name) LIKE  CONCAT(LOWER(:category),'%'))
                            AND (:subCategory IS NULL OR LOWER(sc.name) LIKE CONCAT(LOWER(:subCategory),'%'))
                            AND (:priority IS NULL OR i.priority = :priority)
                            AND (:daysRemain IS NULL OR DATEDIFF(i.priority_date_time , CURRENT_DATE) = :daysRemain)
                            AND (:fromDate IS NULL OR (i.indent_date BETWEEN :fromDate AND :toDate))
                            
                    GROUP BY i.id
                                                                            """;

    String countPendingRfqs="SELECT COUNT(*) FROM ("+getIndentApprovedAndPendingRFqWithSearch+") as total";
}
