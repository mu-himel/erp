package com.agi.aesl.erpscm.pr_indent.repository;

public interface PrIndentQuery {
    String getReadyIndentWithSearch =
            """
                    SELECT pri.id              as          id,
                           pri.category_id     as          categoryId,
                           c.name              as          categoryName,
                           pri.sub_category_id as          subCategoryId,
                           sc.name             as          subCategoryName,
                           COALESCE(SUM(piw.order_qty), 0) as orderQty,
                           COALESCE(sum(piw.pr_qty),0)  as prQty,
                           pri.priority_date        as          priority,
                           pri.product_requirements_ids  as productRequirementIds,
                           piw.warehouse_id   as           warehouseId

                    FROM pr_indents pri
                             LEFT JOIN pr_indent_details prid on pri.id = prid.pr_indent_id
                             LEFT JOIN pr_indent_warehouses piw on piw.pr_indent_detail_id = prid.id
                             LEFT JOIN scm_item_categories c on pri.category_id = c.id
                             LEFT JOIN scm_item_categories sc on pri.sub_category_id = sc.id


                    WHERE pri.status = 'OPEN'
                      AND (:categoryId IS NULL OR c.id = :categoryId)
                      AND (:subCategoryId IS NULL OR sc.id = :subCategoryId)
                      AND (:priority IS NULL OR pri.priority = :priority)

                    GROUP BY pri.id
                                                                                """;

    String countReadyPrIndentWithSearch =
            """
                    select count(*)
                    from (
                    """ +
                    getReadyIndentWithSearch
                    + """
                        ) as temp
                    """;

    String getPrIndentByIdWithSearch =
            """
                    SELECT pri.id                                             as id,
                           prid.id                                            as prDetailId,                                                                                             
                           piw.warehouse_id                                   as warehouseId,
                           piw.id                                             as piwId,
                           pipdt.id                                           as pdId,
                           pipdt.pd_date                                      as pdDate,
                           pipdt.qty                                          as pdQty,
                           prid.attribute                                     as prAttribute,
                           pri.category_id                                    as categoryId,
                           c.name                                             as categoryName,
                           pri.sub_category_id                                as subCategoryId,
                           sc.name                                            as subCategoryName,
                           prid.product_requirements_ids                       as productRequirementsIds,  
                           COALESCE(SUM(piw.order_qty), 0)                    as orderQty,
                           COALESCE(SUM(piw.pr_qty), 0)                       as prQty,
                           pri.priority                                       as priority,
                           pri.priority_date                                  as priorityDate,
                           DATEDIFF(pri.priority_date,CURRENT_DATE)           as daysRemain,
                           (select name from scm_warehouses WHERE id = piw.warehouse_id)    as warehouseName,
                           prid.brand_id                                      as brandId,
                           (select name from scm_category_brands cb WHERE cb.id = prid.brand_id) as brandName

                    FROM pr_indents pri
                             LEFT JOIN pr_indent_details prid on pri.id = prid.pr_indent_id
                             LEFT JOIN pr_indent_warehouses piw ON piw.pr_indent_detail_id = prid.id
                             LEFT JOIN pr_indent_partial_delivery_times pipdt ON pipdt.pr_indent_warehouse_detail_id = piw.id
                             LEFT JOIN scm_item_categories c on pri.category_id = c.id
                             LEFT JOIN scm_item_categories sc on pri.sub_category_id = sc.id
                    WHERE pri.status = 'OPEN'
                      AND (:id IS NOT NULL AND pri.id = :id)
                    group by piw.id,pipdt.id
                """;

    String getPrIndentByIdsWithSearch =
            """
                SELECT          pri.id                                             as id,
                                prid.id                                            as prDetailId,
                                pri.category_id                                    as categoryId,
                                c.name                                             as categoryName,
                                prid.attribute                                     as prAttribute,
                                piw.warehouse_id                                   as warehouseId,
                                piw.id                                             as piwId,
                                pipdt.id                                           as pdId,
                                pipdt.pd_date                                      as pdDate,
                                pipdt.qty                                          as pdQty,
                                pri.sub_category_id                                as subCategoryId,
                                sc.name                                            as subCategoryName,
                                GROUP_CONCAT(pri.product_requirements_ids)         as productRequirementsIds,
                                COALESCE(SUM(piw.order_qty), 0)                    as orderQty,
                                COALESCE(SUM(piw.pr_qty), 0)                       as prQty,
                                pri.priority                                       as priority,
                                pri.priority_date                                  as priorityDate,
                                DATEDIFF(pri.priority_date,CURRENT_DATE)           as daysRemain,
                                (select name from scm_warehouses WHERE id = piw.warehouse_id)    as warehouseName,
                                prid.brand_id                                      as brandId,
                                (select name from scm_category_brands cb WHERE cb.id = prid.brand_id) as brandName
                        FROM pr_indents pri
                                LEFT JOIN pr_indent_details prid on pri.id = prid.pr_indent_id
                                LEFT JOIN pr_indent_warehouses piw ON piw.pr_indent_detail_id = prid.id
                                LEFT JOIN pr_indent_partial_delivery_times pipdt ON pipdt.pr_indent_warehouse_detail_id = piw.id
                                LEFT JOIN scm_item_categories c on pri.category_id = c.id
                                LEFT JOIN scm_item_categories sc on pri.sub_category_id = sc.id
                        
                        WHERE pri.status = 'OPEN'
                        AND (pri.id is not null)
                        AND (pri.id in :ids)
                        GROUP BY pipdt.id,piw.id
                        ORDER BY prid.id ASC
                """;

    String getDemandWithSearch = """
            select d.id as id,
                   d.demand_no as demandNo,
                   d.demand_date as demandDate,
                   sum(dd.request_quantity) itemQty,
                   e.name as employeeName,
                   e.department_name as departmentName
            FROM scm_demand_details dd left join demands d on d.id = dd.demand_id
            LEFT join acl_users e on e.id = d.requested_by_id  
            where  dd.id in(select demand_detail_id
            from product_requirements pr
            where  pr.id in (:prIds)
            AND pr.status = 'OPEN')
            group by d.id
            """;
}
