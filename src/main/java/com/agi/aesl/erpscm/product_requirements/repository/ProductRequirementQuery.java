package com.agi.aesl.erpscm.product_requirements.repository;

public interface ProductRequirementQuery {
    
    String getProductRequirementWithSearch="""
        SELECT GROUP_CONCAT(p.id)                                 as productRequirementIds,
                c.id                                               AS categoryId,
                c.name                                             AS categoryName,
                sc.id                                              AS subCategoryId,
                sc.name                                            AS subCategoryName,
                MIN(demand_deadline)                               as demandDeadline,
                DATEDIFF(MIN(demand_deadline), CURRENT_DATE)       as daysRemain,

                (SELECT COUNT(attr) FROM(
                    SELECT GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by sdda.id asc separator ' - ') attr
                    FROM scm_demand_details ddsq1
                    LEFT JOIN scm_demand_detail_attributes sdda ON sdda.demand_detail_id = ddsq1.id
                    where ddsq1.id IN (p.demand_detail_id) 
                    AND ddsq1.item_parent_category_id = p.category_id
                    and ddsq1.item_category_id = p.sub_category_id) as p
                ) AS itemsQty

        FROM product_requirements AS p
                LEFT JOIN scm_item_categories c ON p.category_id = c.id
                LEFT JOIN scm_item_categories sc ON p.sub_category_id = sc.id
                LEFT JOIN scm_demands d ON p.demand_id = d.id
                LEFT JOIN scm_demand_details dd ON p.demand_detail_id = dd.id

        WHERE (:categoryId IS NULL OR c.id = :categoryId)
        AND (:subCategoryId IS NULL OR sc.id = :subCategoryId)
        AND (:startDate IS NULL OR :endDate IS NULL OR p.product_requirement_date BETWEEN :startDate AND :endDate)
        AND p.status = 'OPEN'
        GROUP BY c.id, sc.id
        ORDER BY c.id, sc.id
            """;

    String countProductRequirementWithSearch =
            """
                    select count(*)
                    from ( 
                                        
                    """ +
                    getProductRequirementWithSearch
                    + """
                         
                         ) as prs
                    """;

    String getProductRequirementViewWithSearch =
    """
                SELECT 
                
                        CASE WHEN count(r.itemName)>1 THEN
                			GROUP_CONCAT(r.pid)
                			ELSE
                			  r.pid
                			END as productRequirementsIds,
                		count(r.itemName) as itemCount,
                		r.categoryId,
                		r.categoryName,
                		r.subCategoryId,
                		r.subCategoryName,
                		r.itemId,
                		r.demandPriority,
                		r.warehouses,
                		r.warehouseIds,
                		r.approvedQty,
                		r.itemName,
                		r.prQty,
                		r.itemsQty,
                		r.currentStock,
                		r.safetytStock,
                		r.transitQty,
                		r.demandDeadline,
                		r.daysRemain,
                		r.brandId,
                		r.brandName
                
                
                FROM (SELECT 
                            
                                p.id                     as pid,
                                c.id                                                                   as categoryId,
                                c.name                                                                 as categoryName,
                                sc.id                                                                  as subCategoryId,
                                sc.name                                                                as subCategoryName,
                                i.id                                                                   as itemId,
                                p.demand_priority                                                      as demandPriority,
                                p.demand_id as demandId,
                                GROUP_CONCAT(w.name)                                                   as warehouses,
                                GROUP_CONCAT(w.id)                                                     as warehouseIds,
                                dd.approved_quantity                                     as approvedQty,
                                (SELECT 
                                CASE 
                                WHEN dda.id IS NOT NULL THEN CONCAT(
                                    GROUP_CONCAT(DISTINCT TRIM(dda.attribute_type),
                                    ' ',
                                    TRIM(dda.attribute_value) ,
                                    ' ',
                                    TRIM(dda.attribute_unit) order by dda.id asc separator ' - '))   
                                    WHEN dda.id IS NULL AND cb.id IS NOT NULL THEN 
                                    cb.name
                                    WHEN dda.id IS NULL AND cb.id IS NULL THEN 
                                    'item attribute not specified'
                                END as demand_attributes
                                FROM product_requirements pr
                                LEFT JOIN scm_demand_details dd ON dd.id = pr.demand_detail_id
                                LEFT JOIN scm_category_brands cb ON cb.id = dd.brand_id
                                LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id  = dd.id
                                WHERE dd.status IN ('PENDING','PENDING_QC') AND pr.id = p.id
                                GROUP BY dd.id)                                                        as itemName,
                                COALESCE((SELECT sum(prtbl.request_quantity) as prQty
                                
                                FROM (
                                    SELECT pr1.id as pr_id,dd.request_quantity , dd.id,
                                    d.warehouse_id ,
                CASE 
                WHEN dda.id IS NOT NULL THEN 
                CONCAT(
                    GROUP_CONCAT(DISTINCT TRIM(dda.attribute_type),
                                    ' ',
                                    TRIM(dda.attribute_value) ,
                                    ' ',
                                    TRIM(dda.attribute_unit) order by dda.id asc separator ' - '))   
                                    WHEN dda.id IS NULL AND cb.id IS NOT NULL THEN 
                                    cb.name
                                    WHEN dda.id IS NULL AND cb.id IS NULL THEN 
                                    'item attribute not specified'
                END as demand_attributes 
                                    FROM product_requirements pr1
                                    LEFT JOIN scm_demand_details dd ON dd.id = pr1.demand_detail_id 
                                    LEFT JOIN scm_demands d ON d.id = dd.demand_id 
                                    LEFT JOIN scm_category_brands cb ON cb.id=dd.brand_id 
                                    LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id  = dd.id
                                    WHERE dd.status IN ('PENDING','PENDING_QC')
                                    GROUP BY dd.id
                                ) prtbl
                                WHERE prtbl.demand_attributes LIKE CONCAT('%',
            (SELECT CASE 
            WHEN dda.id IS NOT NULL THEN 
            CONCAT(
                GROUP_CONCAT(DISTINCT TRIM(dda.attribute_type),
                ' ',
                TRIM(dda.attribute_value) ,
                ' ',
                TRIM(dda.attribute_unit) order by dda.id asc separator ' - '))   
                WHEN dda.id IS NULL AND cb.id IS NOT NULL THEN 
                cb.name
                WHEN dda.id IS NULL AND cb.id IS NULL THEN 
                'item attribute not specified'
                    END as demand_attributes
                                FROM product_requirements pr
                                LEFT JOIN scm_demand_details dd ON dd.id = pr.demand_detail_id
                                LEFT JOIN scm_category_brands cb ON cb.id = dd.brand_id
                                LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id  = dd.id
                                WHERE dd.status IN ('PENDING','PENDING_QC') AND pr.id = p.id
                                GROUP BY dd.id)
            ,'%') ),0)                                              as prQty,
                        
                                (SELECT COUNT(distinct ddsq1.item_id)
                                from scm_demand_details ddsq1
                                where ddsq1.id in (select id
                                                    from scm_demand_details ddsq2
                                                    where ddsq2.item_parent_category_id = p.category_id
                                                    and ddsq2.item_category_id = p.sub_category_id)) as itemsQty,
                                COALESCE((SELECT SUM(istock.stock_qty)
                                        FROM scm_item_stocks istock
                                        where istock.item_id = dd.item_id), 0)                       as currentStock,
                                COALESCE((SELECT item.stock_threshold_qty
                                        FROM scm_items item
                                        where item.id = dd.item_id), 0)                              as safetytStock,
                                0                                                                      as transitQty,
                                (select MIN(demand_deadline) from product_requirements pr3 WHERE pr3.status='OPEN'
                                AND pr3.category_id=:categoryId AND pr3.sub_category_id=:subCategoryId)    as demandDeadline,
                                DATEDIFF((select MIN(demand_deadline) from product_requirements pr3 WHERE pr3.status='OPEN'
                                AND pr3.category_id=:categoryId AND pr3.sub_category_id=:subCategoryId), CURRENT_DATE)  as daysRemain,
                        dd.brand_id as brandId,
                        (select name from scm_category_brands cb where cb.id = dd.brand_id) as brandName
                        FROM product_requirements as p
                                LEFT JOIN scm_item_categories c on p.category_id = c.id
                                LEFT JOIN scm_item_categories sc on p.sub_category_id = sc.id
                                LEFT JOIN scm_demands d on p.demand_id = d.id
                                LEFT JOIN scm_demand_details dd on p.demand_detail_id = dd.id
                                LEFT JOIN scm_items i on dd.item_id = i.id
                                LEFT JOIN scm_warehouses w on w.id = p.warehouse_id
                        
                        WHERE (:categoryId IS NULL OR c.id = :categoryId)
                            AND (:subCategoryId IS NULL OR sc.id = :subCategoryId)
                            AND p.status = 'OPEN' GROUP BY w.id) r
                        GROUP BY r.categoryId, r.subCategoryId, r.brandName, r.itemName   
            """;

    String getProductRequirementViewWithSearchV2= """
            SELECT   p.id as productRequirementsIds,
                                    w.id as warehouseIds,
                                    w.name as warehouses,
                                    COALESCE((SELECT SUM(istock.stock_qty)
                                            FROM scm_item_stocks istock
                                            where istock.item_id = dd.item_id), 0)   as currentStock,
                                    COALESCE((SELECT item.stock_threshold_qty
                                            FROM scm_items item
                                            where item.id = dd.item_id), 0)  as safetytStock,
                                    0       as transitQty,
                                    (SELECT
                                            CASE
                                            WHEN dda.id IS NOT NULL THEN CONCAT(
                                                GROUP_CONCAT(DISTINCT TRIM(dda.attribute_type),
                                                ' ',
                                                TRIM(dda.attribute_value) ,
                                                ' ',
                                                TRIM(dda.attribute_unit) order by dda.id asc separator ' - '))
                                                WHEN dda.id IS NULL AND cb.id IS NOT NULL THEN
                                                cb.name
                                                WHEN dda.id IS NULL AND cb.id IS NULL THEN
                                                'item attribute not specified'
                                            END as demand_attributes
                                            FROM product_requirements pr
                                            LEFT JOIN scm_demand_details dd ON dd.id = pr.demand_detail_id
                                            LEFT JOIN scm_category_brands cb ON cb.id = dd.brand_id
                                            LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id  = dd.id
                                            WHERE dd.status IN ('PENDING','PENDING_QC') AND pr.id = p.id
                                            GROUP BY dd.id)                                                        as itemName,
                                            (select MIN(demand_deadline) from product_requirements pr3 WHERE pr3.status='OPEN'
                                            AND pr3.category_id=:categoryId AND pr3.sub_category_id=:subCategoryId)    as demandDeadline,
                                            c.id as categoryId,
                                            sc.id as subCategoryId,
                                            c.name as categoryName,
                                            sc.name as subCategoryName,
                                            dd.brand_id as brandId,
                                             CASE
            	                                WHEN dd.brand_id IS NOT NULL THEN
                                            	(SELECT name FROM scm_category_brands scb where scb.id=dd.brand_id)
                                            	ELSE ''
                                            	END
                                            	 as brandName,
                                            (dd.request_quantity) as itemsQty,
                                            (dd.pr_qty) as prQty,
                                            DATEDIFF((select MIN(demand_deadline) from product_requirements pr3 WHERE pr3.status='OPEN'
                                            AND pr3.category_id=:categoryId AND pr3.sub_category_id=:subCategoryId), CURRENT_DATE)  as daysRemain
                                    FROM product_requirements as p
                                            LEFT JOIN scm_item_categories c on p.category_id = c.id
                                            LEFT JOIN scm_item_categories sc on p.sub_category_id = sc.id
                                            LEFT JOIN scm_demands d on p.demand_id = d.id
                                            LEFT JOIN scm_demand_details dd on p.demand_detail_id = dd.id
                                            LEFT JOIN scm_items i on dd.item_id = i.id
                                            LEFT JOIN scm_warehouses w on w.id = p.warehouse_id
                                    WHERE (:categoryId IS NULL OR c.id = :categoryId)
                                        AND (:subCategoryId IS NULL OR sc.id = :subCategoryId)
            """;

    String getWarehouseRequirements = """
            SELECT p.id as id,
                   p.stockThresholdQty as stockThresholdQty,
                   p.itemAttribute as itemAttribute,
                   p.warehouseId as warehouseId,
                   p.warehouseName as warehouseName,
                   p.warehouseStoreId as warehouseStoreId,
                   p.name as name,
                   p.stockQty as stockQty,
                   p.prQty as prQty,
                   p.inTransit as inTransit
                FROM    (
                    SELECT  i1.id, 
                    sum(stock_threshold_qty) stockThresholdQty, 
                    iattrs as itemAttribute, 
                    warehouse_id as warehouseId,
                    warehouseName as warehouseName,
                    warehouse_store_id as warehouseStoreId,
                    name as name,
                    SUM(stock_qty) as stockQty,
                    (
                            SELECT SUM(prtbl.pr_qty) as prQty
                            FROM (
                                    SELECT pr.id as pr_id,dd.request_quantity,COALESCE(dd.pr_qty,0) as pr_qty,dd.id,
                                    d.warehouse_id ,
                                           CASE WHEN cb.id IS NOT NULL THEN
                                              GROUP_CONCAT(cb.name,' ',TRIM(dda.attribute_type),' ',TRIM(dda.attribute_value) , ' ',TRIM(dda.attribute_unit) separator ' - ')
                                           ELSE
                                              GROUP_CONCAT(TRIM(dda.attribute_type),' ',TRIM(dda.attribute_value) , ' ',TRIM(dda.attribute_unit) separator ' - ')
                                           END as demand_attributes
                                    FROM product_requirements pr 
                                    LEFT JOIN scm_demand_details dd ON dd.id = pr.demand_detail_id 
                                    LEFT JOIN scm_demands d ON d.id = dd.demand_id 
                                    LEFT JOIN scm_category_brands cb ON cb.id=dd.brand_id 
                                    LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id  = dd.id
                                    WHERE pr.status = 'OPEN' AND dd.status IN ('PENDING','PENDING_QC')
                                    GROUP BY dd.id
                            ) prtbl
                            WHERE prtbl.warehouse_id = i2.warehouse_id
                            AND  (prtbl.demand_attributes IS NULL OR prtbl.demand_attributes LIKE CONCAT('%',:attribute,'%'))
                    ) as prQty,
                    0 as inTransit 

            FROM (
                    SELECT i.id,i.stock_threshold_qty,
                    CONCAT(TRIM(cb2.name),' ',GROUP_CONCAT(TRIM(ia.attribute_type),' ',TRIM(ia.attribute_value) , ' ',TRIM(ia.attribute_unit) ORDER BY ia.id separator ' - ')) iattrs
                    FROM scm_item_attributes ia 
                    LEFT JOIN scm_items i ON i.id = ia.item_id 
                    LEFT JOIN scm_category_brands cb2 ON cb2.id = i.brand_id 
                    WHERE i.active=true
                    GROUP BY i.id
            ) i1
            LEFT JOIN (
                    SELECT i.id,is2.warehouse_id,
                    w.name as warehouseName,
                    is2.warehouse_store_id, i.name,sum(is2.stock_qty) stock_qty
                    FROM scm_item_stocks is2
                    LEFT JOIN scm_items i ON i.id = is2.item_id 
                    LEFT JOIN scm_warehouses w ON w.id = is2.warehouse_id 
                    WHERE i.active = true
                    GROUP BY is2.warehouse_id,warehouse_store_id,i.id
            ) i2 ON i1.id = i2.id
            WHERE iattrs LIKE CONCAT('%',:attribute,'%')
            GROUP BY warehouse_id
            ) as p
            WHERE p.prQty>0
                """;

    String getDemandWithSearch = """
            SELECT d.id as id,
                   d.demand_no as demandNo,
                   d.demand_date as demandDate,
                   sum(dd.pr_qty) itemQty,
                   sum(dd.approved_quantity) approvedQty,
                   e.employee_name as employeeName,
                   e.department_name as departmentName,
                   w.name as warehouseName
            FROM scm_demand_details dd 
            LEFT JOIN scm_demands d on d.id = dd.demand_id
            LEFT JOIN scm_warehouses w on w.id = d.warehouse_id
            LEFT JOIN acl_users e on e.id = d.requested_by_id    
            WHERE  dd.id IN (
                SELECT demand_detail_id
                    FROM product_requirements pr
                    WHERE  pr.id IN (:prIds)
            )
            GROUP BY d.id
            """;
}
