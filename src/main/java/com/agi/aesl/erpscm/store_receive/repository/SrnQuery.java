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
                                       WHERE grn.grn_status IN (:status)
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;


    String getPendingVerifications= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,\s
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
                                                       where d.demand_attributes=p.itemAttributes) as demands,
                                                       srn.srn_status as srnStatus,
                                                       
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,grn.grn_status grnStatus,
                                           grn.is_received_by_store isReceivedByStore,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1\s
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id\s
                                                                   where grn1.id=grn.id)  as items, 
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2\s
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id\s
                                                                   where grn2.id=grn.id)  as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id\s
                                       WHERE srn.srn_status IN (:status)
                                       AND srn.next_verifier_id = :nextVerifierId
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;

    String getPendingApprovals= """
            select p.id,p.grnNo,p.srnId,p.createdAt,p.grnStatus,p.isReceivedByStore,p.categoryName,\s
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
                                                       where d.demand_attributes=p.itemAttributes) as demands,
                                                       srn.srn_status as srnStatus,
                                                       
                                       FROM(SELECT grn.id as id,
                                           srn.id as srnId,
                                           grn.created_at as createdAt,
                                           grn.grn_no as grnNo,grn.grn_status grnStatus,
                                           grn.is_received_by_store isReceivedByStore,
                                           ic.name as categoryName,
                                           i.brand_id as brandId,
                                           (select count(grid1.id) from good_receive_notes grn1\s
                                                                   left join good_receive_item_details grid1 on grn1.id = grid1.good_receive_note_id\s
                                                                   where grn1.id=grn.id)  as items, 
                                           (select sum(grid2.receive_qty) from good_receive_notes grn2\s
                                                                   left join good_receive_item_details grid2 on grn2.id = grid2.good_receive_note_id\s
                                                                   where grn2.id=grn.id)  as receivedQty,
                                           GROUP_CONCAT(ia.attribute_type,' ',ia.attribute_value , ' ',ia.attribute_unit separator ' - ') itemAttributes
                                       FROM good_receive_notes grn
                                       LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                                       LEFT JOIN scm_items i ON i.id = grid.item_id
                                       LEFT JOIN scm_item_attributes ia ON ia.item_id = i.id
                                       LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                                       LEFT JOIN store_receive_notes srn on srn.grn_id = grn.id\s
                                       WHERE srn.srn_status IN (:status)
                                       AND srn.next_approver_id = :nextApproverId
                                       AND (COALESCE(:fromDate) IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                                       GROUP BY grn.id) p
            """;


    String countAll="SELECT COUNT(*) FROM ("+getAll+") as total";
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
            p.name  as employeeName,
            p.departmentName as departmentName,
            p.demand_date as  demandDate,
            p.items as items,
            p.demand_attributes as demandAttributes
            FROM (SELECT 
                d.id,
                d.demand_no,
                e.name,
                dept.name as departmentName,
                d.demand_date,
                COALESCE ((select count(*) from demand_details dd2 where dd2.demand_id = d.id),0) as items,
                GROUP_CONCAT(dda.attribute_type,' ',dda.attribute_value , ' ',dda.attribute_unit separator ' - ') demand_attributes 
            FROM demand_details dd
            LEFT JOIN scm_demand_detail_attributes dda ON dda.demand_detail_id = dd.id
            LEFT JOIN scm_demands d ON d.id = dd.demand_id
            LEFT JOIN acl_users e ON e.id=d.requested_by_id
            LEFT JOIN scm_department dept ON dept.id=e.department_id
            LEFT JOIN store_receive_details srd ON dd.item_id = srd.item_id
            LEFT JOIN store_receive_notes srn ON srn.id = srd.store_receive_note_id
            WHERE dd.status IN ('PENDING')
            GROUP BY srd.item_id, d.id) p
            WHERE p.demand_attributes LIKE CONCAT('%',:attributes,'%')
            """;

    interface PendingDemandList{

        Long getId();
        String getDemandNo();
        String getEmployeeName();
        String getDepartmentName();
        LocalDate getDemandDate();
        Long getItems();
    }
}
