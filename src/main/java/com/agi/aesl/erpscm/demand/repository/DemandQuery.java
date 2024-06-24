package com.agi.aesl.erpscm.demand.repository;

interface DemandQuery {

    String demandDetailQuery= """
            SELECT
            	i.id as id,
            	dd.id as demandDetailId,
                (SELECT count(*) as pr_qty  FROM product_requirements pr WHERE pr.status != 'OPEN' AND pr.demand_detail_id = dd.id) as prQty,
                (SELECT count(*) as pr_qty  FROM product_requirements pr WHERE pr.demand_detail_id = dd.id) as openPrQty,
                d.id as demandId,
                d.next_verifier_id as nextVerifierId,
                d.next_approver_id as nextApproverId,
                d.reviewer_id as reviewerId,
                d.demand_date as demandDate,
                d.demand_no as demandNo,
                d.status as demandStatus,
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
                    FROM items i3 WHERE i3.item_category_id = dd.item_category_id),0)
                WHEN dd.brand_id IS NULL THEN
                    COALESCE((SELECT sum(stockThresholdQty) as stockThresholdQty FROM (
                        SELECT
                            sum(distinct i2.stock_threshold_qty) as stockThresholdQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM item_attributes ia
                        LEFT JOIN items i2 ON i2.id = ia.item_id
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
                        FROM item_attributes ia
                        LEFT JOIN items i2 ON i2.id = ia.item_id
                        WHERE i2.brand_id = dd.brand_id AND i2.active = 1
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')),0)
                END as stockThresholdQty,
                e.id as empId,
                e.employee_id as employeeId,
                e.name as employeeName,
                re.name as reportingManager,
                department.name as department,
                rn.name as designation,
                ic.name as category,
                ic.code as categoryCode,
                ic2.name as parentCategory,
                ic2.code as parentCategoryCode,
                CASE WHEN dda.id IS NULL THEN
                    (SELECT COALESCE(sum(is2.stock_qty),0) as stockQty FROM item_stocks is2 WHERE is2.item_id IN (
                        SELECT i.id from items i WHERE i.item_category_id = dd.item_category_id
                    ) AND is2.warehouse_id = d.warehouse_id)
                WHEN dd.brand_id  IS NULL THEN
                    COALESCE((
                        SELECT sum(stockQty) as stockQty FROM (
                        SELECT
                            (
                                SELECT sum(stock_qty) FROM item_stocks is1 where is1.item_id = ia.item_id 
                                AND is1.warehouse_id = d.warehouse_id 
                            ) stockQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM item_attributes ia
                        LEFT JOIN items i2 ON i2.id = ia.item_id
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
                                SELECT sum(stock_qty) FROM item_stocks is1 where is1.item_id = ia.item_id 
                                AND is1.warehouse_id = d.warehouse_id 
                            ) stockQty,
                            ia.item_id,
                            GROUP_CONCAT(DISTINCT attribute_type,' ',attribute_value , ' ',attribute_unit order by ia.id asc separator ' - ') item_attributes
                        FROM item_attributes ia
                        LEFT JOIN items i2 ON i2.id = ia.item_id
                        WHERE i2.brand_id = dd.brand_id AND i2.active = 1
                        GROUP BY ia.item_id ) stockResulSet
                         WHERE stockResulSet.item_attributes\s
                            LIKE CONCAT('%',GROUP_CONCAT(attribute_type,' ',attribute_value ,
                                ' ',attribute_unit separator ' - '),'%')),0)
                
                END as currentStockQty,
            	0 as inTransit,
            	GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') deamndAttributes
            FROM demand_details dd
            LEFT JOIN demand_detail_attributes dda on dda.demand_detail_id = dd.id
            LEFT JOIN demands d on d.id=dd.demand_id
            LEFT JOIN category_brands cb on cb.id = dd.brand_id
            LEFT JOIN items i on i.id = dd.item_id
            LEFT JOIN item_categories ic on ic.id = dd.item_category_id
            LEFT JOIN item_categories ic2 on ic2.id = dd.item_parent_category_id
            LEFT JOIN employees e on e.id = d.requested_by_id
            LEFT JOIN department on department.id = e.department_id
            LEFT JOIN role_node rn on rn.id = e.role_node_id
            LEFT JOIN employees re on e.reporting_manager_id = re.id
            LEFT JOIN warehouses w ON w.id = d.warehouse_id
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
           d.requestedBy as requestedBy
       FROM Demand d 
        LEFT JOIN d.requestedBy r 
        LEFT JOIN d.warehouse w 
        LEFT JOIN d.demandDetails dd 
        LEFT JOIN dd.item i 
        LEFT JOIN dd.itemCategory c 
        WHERE d.status NOT IN ('PENDING_APPROVAL','PENDING_VERIFICATION','REJECTED','RECEIVED','COMPLETED','CANCELED','DECLINED')
        AND (:warehouseId IS NULL OR w.id = :warehouseId) 
        AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate))
        GROUP BY d.id 
        """;

    String countAllPending = """
        SELECT count(d) FROM Demand d
        LEFT JOIN d.requestedBy r
        LEFT JOIN d.warehouse w
        WHERE d.status NOT IN ('PENDING_APPROVAL','REJECTED','PENDING_VERIFICATION','RECEIVED','COMPLETED','CANCELED','DECLINED')
        AND (:warehouseId IS NULL OR w.id = :warehouseId) 
        AND (:fromDate IS NULL OR (d.demandDate BETWEEN :fromDate AND :toDate)) 
        GROUP BY d.id """;


    String getAllPendingVerification = """
        SELECT 
            d.id as id,
            d.demand_no as demandNo,
            d.status as status,
            (SELECT demand_status FROM `demand_verification_approval_histories` 
                    where `employee_id` = :nextVerifierId AND `demand_id`=d.id) as demandStatus,
            d.demand_date as demandDate,
            pc.name as category,
            COUNT(dd.id) as itemsQty,
            CONCAT(e.employee_id,'-',e.employee_name) as requestedBy 
        FROM demands d 
        LEFT JOIN acl_users e ON e.id = d.requested_by_id
        LEFT JOIN warehouses w ON w.id = d.warehouse_id
        LEFT JOIN demand_details dd ON dd.demand_id = d.id
        LEFT JOIN item_categories ic ON d.item_category_id
        LEFT JOIN item_categories pc ON d.item_parent_category_id
        LEFT JOIN demand_verification_approval_histories dvah ON dvah.demand_id = d.id
        WHERE (ic.id IN (:categories) OR pc.id IN (:categories)) 
        AND ((d.next_verifier_id = :nextVerifierId AND d.status IN (:pendingVerification)) 
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
            (SELECT demand_status FROM `demand_verification_approval_histories` 
                    where `employee_id` = :nextApproverId AND `demand_id`=d.id) as demandStatus,
            d.demand_date as demandDate,
            pc.name as category,
            COUNT(dd.id) as itemsQty,
            CONCAT(e.employee_id,'-',e.employee_name) as requestedBy  
        FROM demands d 
        LEFT JOIN acl_users e ON e.id = d.requested_by_id 
        LEFT JOIN warehouses w ON w.id = d.warehouse_id 
        LEFT JOIN demand_details dd ON dd.demand_id = d.id 
        LEFT JOIN item_categories ic ON ic.id = dd.item_category_id
        LEFT JOIN item_categories pc ON pc.id = dd.item_parent_category_id
        LEFT JOIN demand_verification_approval_histories dvah ON dvah.demand_id = d.id
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
        LEFT JOIN item_categories ic ON d.item_category_id
        LEFT JOIN item_categories pc ON d.item_parent_category_id
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