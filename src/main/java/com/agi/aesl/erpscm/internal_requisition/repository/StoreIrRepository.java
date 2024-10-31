package com.agi.aesl.erpscm.internal_requisition.repository;

import com.agi.aesl.erpscm.internal_requisition.entity.StoreIR;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StoreIrRepository extends JpaRepository<StoreIR,Long> {
    String pendingIr = """
        SELECT sirs.id as id,
        ir.delivery_date as deliveryDate,
        ir.internal_requisition_no as internalRequisitionNo,
        CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
        c.name as categoryName,
        count(ird.id) as itemsQty,
        sirs.ir_status as status,
        sirs.created_at as createdAt,
        w.name as warehouse
    FROM store_irs sirs
    LEFT JOIN internal_requisitions ir ON ir.id = sirs.ir_id
    LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
    LEFT JOIN acl_users e ON e.id = ir.requested_by_id
    LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
    LEFT JOIN internal_requisition_warehouses irw ON irw.internal_requisition_detail_id = ird.id
    LEFT JOIN scm_item_categories c ON c.id = ir.category_id
    WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
    AND irw.to_warehouse_id = :warehouseId 
    AND sirs.ir_status IN ('PENDING','REVIEW','PENDING_VERIFICATION','PENDING_APPROVAL')
    AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
    GROUP BY ir.id
            """;

    String pendingIrVerification = """

        SELECT sirs.id as id,
        ir.internal_requisition_no as internalRequisitionNo,
        CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
        c.name as categoryName,
        count(ird.id) as itemsQty,
        sirs.ir_status as status,
        sirs.created_at as createdAt,
        w.name as warehouse
    FROM store_irs sirs
    LEFT JOIN internal_requisitions ir ON ir.id = sirs.ir_id
    LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
    LEFT JOIN acl_users e ON e.id = ir.requested_by_id
    LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
    LEFT JOIN internal_requisition_warehouses irw ON irw.internal_requisition_detail_id = ird.id
    LEFT JOIN scm_item_categories c ON c.id = ir.category_id
    WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
    AND irw.to_warehouse_id = :warehouseId AND sirs.next_verifier_id = :nextVerifierId 
    AND sirs.ir_status IN ('PENDING_VERIFICATION','REVIEW')
    GROUP BY ir.id
            """;

    String pendingIrApproval = """
            SELECT sirs.id as id,
            ir.internal_requisition_no as internalRequisitionNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            sirs.ir_status as status,
            sirs.created_at as createdAt,
            w.name as warehouse
        FROM store_irs sirs
        LEFT JOIN internal_requisitions ir ON ir.id = sirs.ir_id
        LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN internal_requisition_warehouses irw ON irw.internal_requisition_detail_id = ird.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
        AND irw.to_warehouse_id = :warehouseId AND sirs.next_approver_id = :nextApproverId
        AND sirs.ir_status IN ('PENDING_APPROVAL','REVIEW')
        GROUP BY ir.id
                """;

    String readyForTransfer="""
            SELECT sirs.id as id,
            ir.internal_requisition_no as internalRequisitionNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            (SELECT CASE WHEN p.totalTransfer = 0 AND p.totalCount > 0 then 'PENDING' 
            WHEN p.totalTransfer!=p.totalCount THEN 'PARTIAL' 
            WHEN p.totalTransfer=p.totalCount THEN 'DONE'
            END status FROM (
select count(*) totalCount, count(irw2.transfer_qty) totalTransfer from internal_requisition_details ird2 
JOIN internal_requisition_warehouses irw2 ON irw2.internal_requisition_detail_id = ird2.id 
WHERE ird2.ir_id = ir.id) p) as status,
            sirs.created_at as createdAt,
            w.name as warehouse
        FROM store_irs sirs
        LEFT JOIN internal_requisitions ir ON ir.id = sirs.ir_id
        LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN internal_requisition_warehouses irw ON irw.internal_requisition_detail_id = ird.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
        AND irw.to_warehouse_id = :warehouseId AND sirs.ir_status IN ('VERIFIED','APPROVED')
        GROUP BY ir.id
            """;

    String receiveReq="""
                SELECT sirs.id as id,
                ir.internal_requisition_no as internalRequisitionNo,
                c.name as categoryName,
                count(irw.transfer_qty) as itemsQty,
                sirs.created_at as createdAt,
                w.name as warehouse
                FROM store_irs sirs
                LEFT JOIN internal_requisitions ir ON ir.id = sirs.ir_id
                LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
                LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
                LEFT JOIN internal_requisition_warehouses irw ON irw.internal_requisition_detail_id = ird.id
                LEFT JOIN scm_item_categories c ON c.id = ir.category_id
                WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
                AND irw.from_warehouse_id = :warehouseId AND irw.transfer_qty IS NOT NULL
                AND irw.in_transit IS NOT NULL
                AND sirs.ir_status IN ('VERIFIED','APPROVED')
                GROUP BY ir.id
                """;

    String countPendingIr = "SELECT count(*) FROM ("+pendingIr+")";
    String countPendingIrVerification = "SELECT count(*) FROM ("+pendingIrVerification+")";
    String countPendingIrApproval = "SELECT count(*) FROM ("+pendingIrApproval+")";
    String countReadyForTransfer = "SELECT count(*) FROM ("+readyForTransfer+")";
    String countReceiveReq = "SELECT count(*) FROM ("+receiveReq+")";
    @Query(value = pendingIr, countQuery = countPendingIr, nativeQuery = true)
    Page<PendingStoreIR> findAllPendingIrs(Long warehouseId,LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query(value = readyForTransfer, countQuery = countReadyForTransfer, nativeQuery = true)
    Page<PendingStoreIR> findAllReadyForTransfer(Long warehouseId, Pageable pageable);

    @Query(value = pendingIrVerification, countQuery = countPendingIrVerification, nativeQuery = true)
    Page<PendingStoreIR> findAllPendingIrsVerification(Long warehouseId, String nextVerifierId, Pageable pageable);

    @Query(value = pendingIrApproval, countQuery = countPendingIrApproval, nativeQuery = true)
    Page<PendingStoreIR> findAllPendingIrsApproval(Long warehouseId, String nextApproverId, Pageable pageable);

    @Query(value = receiveReq, countQuery = countReceiveReq, nativeQuery = true)
    Page<PendingStoreIR> findAllReceiveRequisitions(Long warehouseId, Pageable pageable);

    interface PendingStoreIR{
        Long getId();

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime getDeliveryDate();
        String getInternalRequisitionNo();
        String getEmployeeName();
        String getCategoryName();
        Integer getItemsQty();
        String getStatus();

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getCreatedAt();
        String getWarehouse();
    }
}
