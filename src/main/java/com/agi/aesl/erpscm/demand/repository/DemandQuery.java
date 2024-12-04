package com.agi.aesl.erpscm.demand.repository;

interface DemandQuery {

    String prSql = "(SELECT count(*) as pr_qty  FROM product_requirements pr WHERE pr.status != 'OPEN' AND pr.demand_detail_id = dd.id) ";
    String openPr="(SELECT count(*) as pr_qty  FROM product_requirements pr WHERE pr.demand_detail_id = dd.id)";
    String demandDetailQuery= """
            SELECT
            	i.id as id,
            	dd.id as demandDetailId,
                """+prSql+"""
                     as prQty,
                """+openPr+"""
                     as openPrQty,
                d.id as demandId,
                d.next_verifier_id as nextVerifierId,
                d.next_approver_id as nextApproverId,
                d.reviewer_id as reviewerId,
                d.demand_date as demandDate,
                d.demand_no as demandNo,
                d.status as demandStatus,
                d.is_canceled as isCanceled,
                dd.approved_quantity as approvedQuantity,
                dd.request_quantity as requestQuantity,
                dd.status as demandDetailStatus,
                dd.specification  as specification,
                dd.priority as demandPriority,
                dd.item_category_id as itemCategoryId,
                dd.item_parent_category_id as itemParentCategoryId,
                dd.receive_note as receiveNote,
                dd.store_note as storeNote,
                dd.decline_note as declineNote,
                cb.id as brandId,
                cb.name as brandName,
                GROUP_CONCAT(dda.attribute_type) as attributeTypes,
                GROUP_CONCAT(dda.attribute_value) as attributeValues,
                CASE WHEN dd.brand_id  IS NULL THEN
                  GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ')
                WHEN dd.brand_id IS NOT NULL AND dda.id IS NOT NULL THEN
                  CONCAT(cb.name,' - ',GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - '))
                WHEN dd.brand_id IS NOT NULL AND dda.id IS NULL THEN
                    cb.name
                ELSE
                  GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ')
                END as name,
                w.id as warehouseId,
                w.name as warehouseName,
                w.location as warehouseLocation,
                i.code as code,
                i.item_unit as itemUnit,
                CASE WHEN dda.id IS NULL THEN
                    COALESCE((SELECT COALESCE(sum(distinct i3.stock_threshold_qty),0) as stockThresholdQty
                    FROM scm_items i3 WHERE i3.item_category_id = dd.item_category_id),0)
                WHEN dd.brand_id IS NULL THEN
                    COALESCE((SELECT sum(stockThresholdQty) as stockThresholdQty FROM (
                        SELECT
                            sum(distinct i2.stock_threshold_qty) as stockThresholdQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM scm_item_attributes ia
                        LEFT JOIN scm_items i2 ON i2.id = ia.item_id
                        WHERE i2.active = 1 
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')),0)
                WHEN dd.brand_id IS NOT NULL THEN
                    COALESCE((SELECT sum(stockThresholdQty) as stockThresholdQty FROM (
                        SELECT
                            sum(distinct i2.stock_threshold_qty) as stockThresholdQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM scm_item_attributes ia
                        LEFT JOIN scm_items i2 ON i2.id = ia.item_id
                        WHERE i2.brand_id = dd.brand_id AND i2.active = 1
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')),0)
                END as stockThresholdQty,
                e.id as empId,
                e.employee_id as employeeId,
                e.employee_name as employeeName,
                re.employee_name as reportingManager,
                e.department_name as department,
                e.designation_name as designation,
                ic.name as category,
                ic.code as categoryCode,
                ic2.name as parentCategory,
                ic2.code as parentCategoryCode,
                CASE WHEN dda.id IS NULL THEN
                    (SELECT COALESCE(sum(is2.stock_qty),0) as stockQty FROM scm_item_stocks is2 WHERE is2.item_id IN (
                        SELECT i.id from scm_items i WHERE i.item_category_id = dd.item_category_id
                    ) AND is2.warehouse_id = d.warehouse_id)
                WHEN dd.brand_id  IS NULL THEN
                    COALESCE((
                        SELECT sum(stockQty) as stockQty FROM (
                        SELECT
                            (
                                SELECT sum(stock_qty) FROM scm_item_stocks is1 where is1.item_id = ia.item_id 
                                AND is1.warehouse_id = d.warehouse_id 
                            ) stockQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM scm_item_attributes ia
                        LEFT JOIN scm_items i2 ON i2.id = ia.item_id
                        WHERE i2.active = 1
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')
                        ),0)
                WHEN dd.brand_id IS NOT NULL THEN
                    COALESCE((SELECT sum(stockQty) as stockQty FROM (
                        SELECT
                            (
                                SELECT sum(stock_qty) FROM scm_item_stocks is1 where is1.item_id = ia.item_id 
                                AND is1.warehouse_id = d.warehouse_id 
                            ) stockQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM scm_item_attributes ia
                        LEFT JOIN scm_items i2 ON i2.id = ia.item_id
                        WHERE i2.brand_id = dd.brand_id AND i2.active = 1
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes\s
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')),0)
                
                END as currentStockQty,
                d.delivery_date as deliveryDate,
                (DATEDIFF(d.delivery_date,CURRENT_DATE)) as daysRemain,
            	CASE WHEN dd.item_id IS NOT NULL THEN
            	  (SELECT sum(p.approved_quantity) FROM (SELECT
                        sdd.approved_quantity,
                        scb.name as brand_name,
                        GROUP_CONCAT(DISTINCT sdda.attribute_type,' ',sdda.attribute_value , ' ',sdda.attribute_unit order by sdda.id asc separator ' - ') attribute_name	
                       FROM scm_demand_details sdd
                       LEFT JOIN scm_demand_detail_attributes sdda ON sdda.demand_detail_id = sdd.id
                       LEFT JOIN scm_category_brands scb ON scb.id = sdd.brand_id
                       WHERE sdd.item_id IS NOT NULL
                        AND sdd.status IN ('PENDING_QC')  AND sdd.approved_quantity > 0
                        group by sdd.id
                       ) p
                   GROUP BY p.brand_name, p.attribute_name)
            	ELSE
            	  (SELECT sum(p.approved_quantity) FROM (SELECT
               	sdd.approved_quantity,
               	scb.name as brand_name,
               	GROUP_CONCAT(DISTINCT sdda.attribute_type,' ',sdda.attribute_value , ' ',sdda.attribute_unit order by sdda.id asc separator ' - ') attribute_name	
               FROM scm_demand_details sdd
               LEFT JOIN scm_demand_detail_attributes sdda ON sdda.demand_detail_id = sdd.id
               LEFT JOIN scm_category_brands scb ON scb.id = sdd.brand_id
               WHERE sdd.status IN ('PENDING_QC')  AND sdd.approved_quantity > 0
               	group by sdd.id
               ) p WHERE p.attribute_name like GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ')
               GROUP BY p.brand_name, p.attribute_name)
            	END as inTransit,
            	GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') deamndAttributes
            FROM scm_demand_details dd
            LEFT JOIN scm_demand_detail_attributes dda on dda.demand_detail_id = dd.id
            LEFT JOIN scm_demands d on d.id=dd.demand_id
            LEFT JOIN scm_category_brands cb on cb.id = dd.brand_id
            LEFT JOIN scm_items i on i.id = dd.item_id
            LEFT JOIN scm_item_categories ic on ic.id = dd.item_category_id
            LEFT JOIN scm_item_categories ic2 on ic2.id = dd.item_parent_category_id
            LEFT JOIN acl_users e on e.id = d.requested_by_id
            LEFT JOIN acl_users re on e.reporting_manager_id = re.id
            LEFT JOIN scm_warehouses w ON w.id = d.warehouse_id
            WHERE demand_id = :id
            GROUP BY dda.demand_detail_id
            """;

    String myDemandSql = """
            SELECT 
                d.id as id,
                d.demandNo as demandNo,
                d.status as status,
                d.demandDate as demandDate,
                d.category as category,
                COUNT(dd) as itemsQty,
                d.requestedBy as requestedBy
            FROM Demand d 
            LEFT JOIN d.requestedBy r
            LEFT JOIN d.demandDetails dd
            LEFT JOIN dd.item i
            LEFT JOIN dd.itemCategory c 
            WHERE r.id=:id
            AND d.status NOT IN ('RECEIVED', 'REJECTED','COMPLETED')
            AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate))
            GROUP BY d.id
            """;

    String countMyDemandSql = """
            SELECT count(*) FROM Demand d JOIN d.requestedBy r 
            WHERE r.id=:id
            AND d.status NOT IN ('RECEIVED', 'REJECTED','COMPLETED')
            AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate))
            GROUP BY d.id""";


    String getAllPending ="""
        SELECT 
        d.id as id,
           d.demandNo as demandNo,
           d.status as status,
           d.demandDate as demandDate,
           d.category as category,
           COUNT(dd) as itemsQty,
           d.requestedBy as requestedBy,
           DATEDIFF(d.deliveryDate,CURRENT_DATE) as daysRemain
       FROM Demand d 
        LEFT JOIN d.requestedBy r 
        LEFT JOIN d.warehouse w 
        LEFT JOIN d.demandDetails dd 
        LEFT JOIN dd.item i 
        LEFT JOIN dd.itemCategory c 
        WHERE d.status NOT IN ('PENDING_APPROVAL','PENDING_VERIFICATION','REJECTED','RECEIVED','COMPLETED','CANCELED','DECLINED')
        AND (:warehouseId IS NULL OR w.id = :warehouseId) 
        AND (:demandNo IS NULL OR d.demandNo LIKE CONCAT('%',:demandNo,'%'))
        AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate))
        AND (:daysRemain IS NULL OR DATEDIFF(d.deliveryDate,CURRENT_DATE) <= :daysRemain)
        GROUP BY d.id 
        """;

    String countAllPending = """
        SELECT count(d) FROM Demand d
        LEFT JOIN d.requestedBy r
        LEFT JOIN d.warehouse w
        WHERE d.status NOT IN ('PENDING_APPROVAL','REJECTED','PENDING_VERIFICATION','RECEIVED','COMPLETED','CANCELED','DECLINED')
        AND (:warehouseId IS NULL OR w.id = :warehouseId) 
        AND (:demandNo IS NULL OR d.demandNo LIKE CONCAT('%',:demandNo,'%'))
        AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate)) 
        AND (:daysRemain IS NULL OR DATEDIFF(d.deliveryDate,CURRENT_DATE) <= :daysRemain)
        GROUP BY d.id """;


    String getAllPendingVerification = """
        SELECT 
            d.id as id,
            d.demand_no as demandNo,
            d.status as status,
            (SELECT demand_status FROM `scm_demand_verification_approval_histories` 
                    where `employee_id` = :nextVerifierId AND `demand_id`=d.id AND demand_status='VERIFIED') as demandStatus,
            d.demand_date as demandDate,
            pc.name as category,
            (select count(dd1.id) from scm_demand_details dd1 WHERE dd1.demand_id = d.id) as itemsQty,
            CONCAT(e.employee_id,'-',e.employee_name) as requestedBy 
        FROM scm_demands d 
        LEFT JOIN acl_users e ON e.id = d.requested_by_id
        LEFT JOIN scm_warehouses w ON w.id = d.warehouse_id
        LEFT JOIN scm_demand_details dd ON dd.demand_id = d.id
        LEFT JOIN scm_item_categories ic ON ic.id = d.sub_category_id
        LEFT JOIN scm_item_categories pc ON pc.id = d.category_id
        LEFT JOIN scm_demand_verification_approval_histories dvah ON dvah.demand_id = d.id
        WHERE ((d.next_verifier_id = :nextVerifierId AND d.status IN (:pendingVerification)) 
            OR (dvah.employee_id = :nextVerifierId AND dvah.demand_status='VERIFIED'))
        AND (:fromDate IS NULL OR (d.demand_date BETWEEN :fromDate AND :toDate))
        GROUP BY d.id 
        ORDER BY CASE WHEN d.status IN ('REVIEW','PENDING_VERIFICATION') THEN 1 ELSE 2 END ASC
        """;

    String countAllPendingVerification="SELECT COUNT(*) FROM ("+getAllPendingVerification+") total";


    String getAllCloseDemands = """
        SELECT 
            d.id as id,
            d.demandNo as demandNo,
            d.status as status,
            d.demandDate as demandDate,
            d.category as category,
            COUNT(dd) as itemsQty,
            d.requestedBy as requestedBy 
        FROM Demand d 
        LEFT JOIN d.requestedBy r 
        LEFT JOIN d.warehouse w 
        LEFT JOIN d.demandDetails dd 
        LEFT JOIN dd.item i 
        LEFT JOIN dd.itemCategory c
        WHERE d.status IN ('REJECTED','COMPLETED','CANCELED','DECLINED','RECEIVED')
        AND (:warehouseId IS NULL OR w.id = :warehouseId)
        AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate)) 
        GROUP BY d.id
        """;

    String countAllCloseDemands = """
        SELECT count(d) FROM Demand d
            LEFT JOIN d.requestedBy r 
            LEFT JOIN d.warehouse w 
            WHERE d.status IN ('REJECTED','COMPLETED','CANCELED','DECLINED','RECEIVED')
            AND (:warehouseId IS NULL OR w.id = :warehouseId)
            AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate))
            GROUP BY d.id
        """;

    String getAllPendingApprovalDemands = """
        SELECT d.id as id,
            d.demand_no as demandNo,
            d.status as status,
            (SELECT demand_status FROM `scm_demand_verification_approval_histories` 
                    where `employee_id` = :nextApproverId AND `demand_id`=d.id AND demand_status='APPROVED') as demandStatus,
            d.demand_date as demandDate,
            pc.name as category,
            (select count(dd1.id) from scm_demand_details dd1 WHERE dd1.demand_id = d.id) as itemsQty,
            CONCAT(e.employee_id,'-',e.employee_name) as requestedBy  
        FROM scm_demands d 
        LEFT JOIN acl_users e ON e.id = d.requested_by_id 
        LEFT JOIN scm_warehouses w ON w.id = d.warehouse_id 
        LEFT JOIN scm_demand_details dd ON dd.demand_id = d.id 
        LEFT JOIN scm_item_categories ic ON ic.id = dd.item_category_id
        LEFT JOIN scm_item_categories pc ON pc.id = dd.item_parent_category_id
        LEFT JOIN scm_demand_verification_approval_histories dvah ON dvah.demand_id = d.id
        WHERE ((d.next_approver_id = :nextApproverId AND d.status IN (:demandStatus))
            OR (dvah.employee_id = :nextApproverId AND dvah.demand_status = 'APPROVED')) 
        AND (:fromDate IS NULL OR (d.demand_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY d.id ORDER BY CASE WHEN d.status IN ('REVIEW','PENDING_APPROVAL') THEN 1 ELSE 2 END ASC
        """;

    String countAllPendingApprovalDemands = "SELECT COUNT(*) FROM ("+getAllPendingApprovalDemands+") total";

    String getAllDemandsByCategory = """
        SELECT 
            d.id as id,
            d.demandNo as demandNo,
            d.status as status,
            d.demandDate as demandDate,
            d.category as category,
            COUNT(dd) as itemsQty,
            r.employeeName as requestedBy
        FROM Demand d 
            LEFT JOIN d.requestedBy r 
            LEFT JOIN d.warehouse w 
            LEFT JOIN d.demandDetails dd 
            LEFT JOIN dd.item i 
            LEFT JOIN dd.itemCategory c 
            WHERE (dd.itemCategory.id IN :categories OR dd.itemParentCategory.id IN :categories)
            AND d.status NOT IN ('PENDING_APPROVAL','REJECTED','PENDING_VERIFICATION','RECEIVED','COMPLETED','CANCELED','DECLINED')
            AND (w.id IN :warehouseId) 
            AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate)) 
            AND (:daysRemain IS NULL OR DATEDIFF(d.deliveryDate,CURRENT_DATE) <= :daysRemain)
            GROUP BY d.id
            """;

    String countAllDemandsByCategory="""
        SELECT count(d) FROM Demand d
            LEFT JOIN d.requestedBy r
            LEFT JOIN d.warehouse w
            LEFT JOIN d.demandDetails dd
            LEFT JOIN dd.item i 
            LEFT JOIN dd.itemCategory c 
        WHERE (dd.itemCategory.id IN :categories OR dd.itemParentCategory.id IN :categories)
         AND d.status NOT IN ('PENDING_APPROVAL','REJECTED','PENDING_VERIFICATION','RECEIVED','COMPLETED','CANCELED','DECLINED')
         AND (w.id IN :warehouseId) 
         AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate)) 
         AND (:daysRemain IS NULL OR DATEDIFF(d.deliveryDate,CURRENT_DATE) <= :daysRemain)
         GROUP BY d.id 
            """;

    String getAllFilteredPendingVerificationDemands = """
            SELECT 
            d.id as id,
            d.demand_no as demandNo,
            d.status as status,
            (SELECT demand_status FROM `demand_verification_approval_histories` 
                    where `employee_id` = :nextVerifierId AND `demand_id`=d.id) as demandStatus,
            d.demand_date as demandDate,
            pc.name as category,
            COUNT(dd.id) as itemsQty,
            CONCAT(e.employee_id,'-',e.name) as requestedBy 
        FROM demands d 
        LEFT JOIN employees e ON e.id = d.requested_by_id
        LEFT JOIN warehouses w ON w.id = d.warehouse_id
        LEFT JOIN demand_details dd ON dd.demand_id = d.id
        LEFT JOIN item_categories ic ON ic.id = d.item_category_id
        LEFT JOIN item_categories pc ON pc.id = d.item_parent_category_id
        LEFT JOIN demand_verification_approval_histories dvah ON dvah.demand_id = d.id
        WHERE (ic.id IN (:categories) OR pc.id IN (:categories)) 
        AND ((d.next_verifier_id = :nextVerifierId AND d.status IN (:pendingVerification)) 
            OR (dvah.employee_id = :nextVerifierId AND dvah.demand_status='VERIFIED'))
        AND (:fromDate IS NULL OR (d.demand_date BETWEEN :fromDate AND :toDate))
        GROUP BY d.id 
        ORDER BY CASE WHEN d.status IN ('REVIEW','PENDING_VERIFICATION') THEN 1 ELSE 2 END ASC
                    """;
    String countAllFilteredPendingVerificationDemands = " SELECT COUNT(*) FROM ("+getAllFilteredPendingVerificationDemands+") total";
     
    String getAllFilteredPendingApprovalDemands = """
        SELECT d.id as id,
        d.demand_no as demandNo,
        d.status as status,
        (SELECT demand_status FROM `demand_verification_approval_histories` 
                where `employee_id` = :nextApproverId AND `demand_id`=d.id) as demandStatus,
        d.demand_date as demandDate,
        pc.name as category,
        COUNT(dd.id) as itemsQty,
        CONCAT(e.employee_id,'-',e.name) as requestedBy 
    FROM demands d 
    LEFT JOIN employees e ON e.id = d.requested_by_id
    LEFT JOIN warehouses w ON w.id = d.warehouse_id 
    LEFT JOIN demand_details dd ON dd.demand_id = d.id  
    LEFT JOIN item_categories ic ON ic.id = dd.item_category_id
    LEFT JOIN item_categories pc ON pc.id = dd.item_parent_category_id
    LEFT JOIN demand_verification_approval_histories dvah ON dvah.demand_id = d.id
    WHERE (c.id IN (:categoryIds) OR pc.id IN (:categoryIds)) 
    AND ((d.next_approver_id = :nextApproverId AND d.status IN (:demandStatuses))
        OR (dvah.employee_id = :nextApproverId AND dvah.demand_status = 'APPROVED')) 
    AND (:fromDate IS NULL OR (d.demand_date BETWEEN :fromDate AND :toDate)) 
    GROUP BY d.id ORDER BY CASE WHEN d.status IN ('REVIEW','PENDING_APPROVAL') THEN 1 ELSE 2 END ASC
            """;

    String countAllFilteredPendingApprovalDemands = "SELECT COUNT(*) FROM ("+getAllFilteredPendingApprovalDemands+") total";
}