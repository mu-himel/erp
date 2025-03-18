package com.agi.aesl.erpscm.pr_indent.repository;

public class PrIndentQuery {
    private PrIndentQuery(){}
    public static final String COUNT_START="SELECT COUNT(*) FROM (";
    public static final String COUNT_END=") AS TOTAL";
    public static final String GET_READY_INDENT_WITH_SEARCH =
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
                      AND (COALESCE(:fromDate) IS NULL OR pri.priority_date BETWEEN :fromDate AND :toDate)

                    GROUP BY pri.id
                    """;

    public static final String COUNT_READY_INDENT_WITH_SEARCH =COUNT_START+ GET_READY_INDENT_WITH_SEARCH +COUNT_END;

    public static final String GET_PR_INDENT_BY_ID_WITH_SEARCH =
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

    public static final String GET_PER_INDENT_BY_IDS_WITH_SEARCH = """
                SELECT
                r.id,
                r.brandName,
                r.warehouseName,
                SUM(r.prQty) as prQty,
                r.prDetailId,
                r.categoryId,
                r.categoryName,
                r.prAttribute,
                r.warehouseId,
                r.piwId,
                r.pdId,
                r.pdDate,
                r.pdQty,
                r.subCategoryId,
                r.subCategoryName,
                GROUP_CONCAT(DISTINCT r.productRequirementsIds) as productRequirementsIds,
                SUM(r.orderQty) as orderQty,
                r.priority,
                r.priorityDate,
                r.daysRemain,
                r.brandId
                FROM (
                             SELECT
                                    pri.id                                             as id,
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
                                    GROUP_CONCAT(prid.product_requirements_ids)         as productRequirementsIds,
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
                            ) as r
                GROUP BY r.brandName,r.prAttribute, r.warehouseName
                ORDER BY r.brandName, r.warehouseName
                """;

}
