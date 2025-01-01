package com.agi.aesl.erpscm.store_receive.repository;

import java.time.LocalDate;

public interface SrnQuery {

    String getAll= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,
                                       p.receivedQty,p.itemAttributes,p.brandId,
                                       p.items as items,
                                       (select count(*) FROM (SELECT d.id,d.demand_no ,
                                                       dd.brand_id,
                                                       GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes
                                                       FROM scm_demand_details dd
                                                       LEFT JOIN scm_demands d ON d.id = dd.demand_id
                                                       LEFT JOIN scm_demand_detail_attributes dda on dda.demand_detail_id = dd.id
                                                       WHERE dd.brand_id = p.brandId AND dd.status IN ('PENDING')
                                                       GROUP BY d.id) d
                                                       where d.demand_attributes=p.itemAttributes
                                       ) as demands,
                                       p.srnStatus as srnStatus
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,
                                           grn.grn_status as grnStatus,
                                           srn.srn_status as srnStatus,
                                           grn.is_received_by_store isReceivedByStore,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id
                                                                   where grn1.id=grn.id)  as items, 
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id
                                                                   where grn2.id=grn.id)  as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id
                                       WHERE ((srn.id IS NOT NULL AND srn.srn_status IN (:status)) 
                                       OR (srn.id IS NULL AND grn.grn_status IN ('READY_FOR_STORE') ))
                                       AND grn.warehouse_id = :warehouseId
                                       AND (:grnNo IS NULL OR grn.grn_no LIKE CONCAT('%',:grnNo,'%'))
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;


    String getPendingVerifications= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,
                                       p.receivedQty,p.itemAttributes,p.brandId,
                                       p.items as items,
                                       (select count(*) FROM (SELECT d.id,d.demand_no ,
                                                       dd.brand_id,
                                                       GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes
                                                       FROM scm_demand_details dd
                                                       LEFT JOIN scm_demands d ON d.id = dd.demand_id
                                                       LEFT JOIN scm_demand_detail_attributes dda on dda.demand_detail_id = dd.id
                                                       WHERE dd.brand_id = p.brandId AND dd.status IN ('PENDING')
                                                       GROUP BY d.id) d
                                                       where d.demand_attributes=p.itemAttributes
                                       ) as demands,
                                       p.srnStatus as srnStatus
                                                       
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,grn.grn_status  as grnStatus,
                                           grn.is_received_by_store as isReceivedByStore,
                                           CASE WHEN srn.srn_status != 'REVIEW' AND (svah.id IS NOT NULL 
                                           AND svah.store_receive_note_id = srn.id
                                           AND svah.employee_id = :nextVerifierId
                                           AND svah.srn_status IN ('VERIFIED')) THEN
                                                    svah.srn_status
                                                ELSE
                                                    srn.srn_status
                                           END as srnStatus,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id
                                                                   where grn1.id=grn.id)  as items,
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id
                                                                   where grn2.id=grn.id)  as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id
                                       LEFT JOIN srn_verify_approval_histories svah ON svah.store_receive_note_id = srn.id
                                       WHERE (
                                        (srn.next_verifier_id = :nextVerifierId AND srn.srn_status IN ('PENDING_VERIFICATION','REVIEW','VERIFIED'))
                                        OR
                                        (svah.employee_id = :nextVerifierId AND svah.srn_status = 'VERIFIED')
                                       )
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;

    String getPendingApprovals= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,
                                       p.receivedQty,p.itemAttributes,p.brandId,
                                       p.items as items,
                                       (select count(*) FROM (SELECT d.id,d.demand_no,
                                                       dd.brand_id,
                                                       GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes
                                                       FROM scm_demand_details dd
                                                       LEFT JOIN scm_demands d ON d.id = dd.demand_id
                                                       LEFT JOIN scm_demand_detail_attributes dda on dda.demand_detail_id = dd.id
                                                       WHERE dd.brand_id = p.brandId AND dd.status IN ('PENDING')
                                                       GROUP BY d.id) d
                                                       where d.demand_attributes=p.itemAttributes
                                       ) as demands,
                                       
                                       p.srnStatus as srnStatus
                                                       
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,grn.grn_status grnStatus,
                                           CASE WHEN srn.srn_status != 'REVIEW' AND (svah.id IS NOT NULL 
                                           AND svah.store_receive_note_id = srn.id
                                           AND svah.employee_id = :nextApproverId
                                           AND svah.srn_status IN ('APPROVED')) THEN
                                                    svah.srn_status
                                                ELSE
                                                    srn.srn_status
                                           END as srnStatus,
                                           grn.is_received_by_store isReceivedByStore,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id
                                                                   where grn1.id=grn.id)  as items,
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id
                                                                   where grn2.id=grn.id) as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id
                                       LEFT JOIN srn_verify_approval_histories svah ON svah.store_receive_note_id = srn.id
                                       WHERE (
                                       (srn.next_approver_id = :nextApproverId AND srn.srn_status IN ('PENDING_APPROVAL','REVIEW','APPROVED'))
                                       OR
                                       (svah.employee_id = :nextApproverId AND svah.srn_status = 'APPROVED')
                                       )
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;

    String getAllCompleted= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,
                                       p.receivedQty,p.itemAttributes,p.brandId,
                                       p.items as items,
                                       (select count(*) FROM (SELECT d.id,d.demand_no ,
                                                       dd.brand_id,
                                                       GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes
                                                       FROM scm_demand_details dd
                                                       LEFT JOIN scm_demands d ON d.id = dd.demand_id
                                                       LEFT JOIN scm_demand_detail_attributes dda on dda.demand_detail_id = dd.id
                                                       WHERE dd.brand_id = p.brandId AND dd.status IN ('PENDING')
                                                       GROUP BY d.id) d
                                                       where d.demand_attributes=p.itemAttributes
                                       ) as demands,
                                       p.srnStatus as srnStatus
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,
                                           grn.grn_status as grnStatus,
                                           srn.srn_status as srnStatus,
                                           grn.is_received_by_store isReceivedByStore,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id
                                                                   where grn1.id=grn.id)  as items, 
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id
                                                                   where grn2.id=grn.id)  as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id
                                       WHERE srn.srn_status IN (:status) AND grn.warehouse_id = :warehouseId
                                       AND (:grnNo IS NULL OR grn.grn_no LIKE CONCAT('%',:grnNo,'%'))
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;


    String countAll="SELECT COUNT(*) FROM ("+getAll+") as total";
    String countAllCompleted="SELECT COUNT(*) FROM ("+getAllCompleted+") as total";
    String countPendingVerifications="SELECT COUNT(*) FROM ("+getPendingVerifications+") as total";
    String countPendingApprovals="SELECT COUNT(*) FROM ("+getPendingApprovals+") as total";

    String getPendingDemandsBySrnForSrnItems = """
            SELECT 
                d.id as id,
                d.demand_no as demandNo,
                e.name as employeeName,
                e.department_name as departmentName,
                d.demand_date as demandDate,
                count(dd.id) as items 
            FROM scm_demand_details dd
            LEFT JOIN scm_demands d ON d.id = dd.demand_id
            LEFT JOIN acl_users e ON e.id=d.requested_by_id
            LEFT JOIN store_receive_details srd ON dd.item_id = srd.item_id
            LEFT JOIN store_receive_notes srn ON srn.id = srd.store_receive_note_id
            WHERE dd.status IN ('PENDING') and srd.item_id=:id
            GROUP BY srd.item_id, d.id
            """;


    String getGetPendingDemandsByAttributes= """
            SELECT
            p.id as id,
            p.demand_no as demandNo,
            p.employee_name  as employeeName,
            p.departmentName as departmentName,
            p.demand_date as  demandDate,
            p.items as items,
            p.warehouseName as warehouseName,
            p.demand_attributes as demandAttributes
            FROM (SELECT 
                d.id, 
                sw.name as warehouseName,
                d.demand_no,
                e.employee_name,
                e.department_name as departmentName,
                d.demand_date,
                COALESCE ((select count(*) from scm_demand_details dd2 where dd2.demand_id = d.id),0) as items,
                GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes 
            FROM scm_demand_details dd
            LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id = dd.id
            LEFT JOIN scm_demands d ON d.id = dd.demand_id
            LEFT JOIN acl_users e ON e.id=d.requested_by_id
            LEFT JOIN store_receive_details srd ON dd.item_id = srd.item_id
            LEFT JOIN store_receive_notes srn ON srn.id = srd.store_receive_note_id
            LEFT JOIN scm_warehouses sw ON sw.id = d.warehouse_id
            WHERE dd.status IN ('PENDING') AND d.warehouse_id IN (:warehouseId)
            GROUP BY srd.item_id, dd.id) p
            WHERE (COALESCE(:attributes) IS NULL OR p.demand_attributes LIKE CONCAT('%',:attributes,'%'))
            GROUP BY p.demand_attributes, p.id
            """;

    interface PendingDemandList{

        Long getId();
        String getDemandNo();
        String getEmployeeName();
        String getWarehouseName();
        String getDepartmentName();
        LocalDate getDemandDate();
        Long getItems();
    }
}
