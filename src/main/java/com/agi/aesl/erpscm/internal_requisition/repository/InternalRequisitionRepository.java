package com.agi.aesl.erpscm.internal_requisition.repository;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.internal_requisition.entity.InternalRequisition;
import com.agi.aesl.erpscm.internal_requisition.entity.InternalRequisitionDetail;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InternalRequisitionRepository extends JpaRepository<InternalRequisition,Long> {
    String allIr = """
        SELECT 
            ir.id as id,
            ir.internal_requisition_no as irNo,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            ir.ir_status as status,
            ir.delivery_date as deliveryDate,
            ir.created_at as createdAt
        FROM internal_requisitions ir 
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING','PENDING_VERIFICATION','PENDING_APPROVAL', 'REVIEW','PROCESSING')
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY ir.id
        """;

    String allClosedIr = """
            SELECT 
                ir.id as id,
                ir.internal_requisition_no as irNo,
                c.name as categoryName,
                count(ird.id) as itemsQty,
                ir.ir_status as status,
                ir.delivery_date as deliveryDate,
                w.name as warehouse
            FROM internal_requisitions ir 
            LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
            LEFT JOIN scm_item_categories c ON c.id = ir.category_id
            LEFT JOIN scm_warehouses w ON w.id=ir.warehouse_id
            WHERE ir.ir_status IN ('RECEIVED', 'REJECTED')
            AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
            GROUP BY ir.id
            """;//    Page<IrListInfo> findAllClosedIr(Pageable pageable);
//
//    Page<?> findAllPendingVerificationIr(String id, Pageable pageable);
//
//    Page<?> findAllPendingApprovalIr(String id, Pageable pageable);
//
//    Page<?> findAllVerifiedOrApprovedIr(Pageable pageable);
//
//    Page<?> findAllProcessedIr(Pageable pageable);

    String allPendingVerificationIr = """
        SELECT 
            ir.id as id,
            ir.internal_requisition_no as irNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            ir.ir_status as status,
            ir.delivery_date as deliveryDate,
            ir.created_at as createdAt
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING_VERIFICATION', 'REVIEW','VERIFIED')
        AND ir.next_verifier_id = :nextVerifierId
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY ir.id
        """;

    String allPendingApprovalIr = """
        SELECT 
            ir.id as id,
            ir.internal_requisition_no as irNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            ir.ir_status as status,
            ir.delivery_date as deliveryDate
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING_APPROVAL', 'REVIEW','APPROVED')
        AND ir.next_approver_id = :nextApproverId
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY ir.id
        """;

    String allVerifiedOrApprovedIr = """
        SELECT 
            ir.id as id,
            ir.internal_requisition_no as irNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            ir.ir_status as status,
            ir.delivery_date as deliveryDate,
            ir.created_at as createdAt,
            w.name as warehouse
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND (ir.is_processed IS NULL OR ir.is_processed=0)
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY ir.id
        """;

    String allProcessedIr = """
        SELECT 
            ir.id as id,
            ir.internal_requisition_no as irNo,
            CONCAT(e.employee_id, '-' , e.employee_name) as employeeName,
            c.name as categoryName,
            count(ird.id) as itemsQty,
            ir.ir_status as status,
            ir.delivery_date as deliveryDate,
            w.name as warehouse
        FROM internal_requisitions ir 
        LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate)) 
        GROUP BY ir.id
        """;

    String countAllIr = " SELECT count(*) FROM ("  + allIr + " ) as total";
    String countAllClosedIr = " SELECT count(*) FROM ("  + allClosedIr + " ) as total";
    String countAllPendingVerificationIr = " SELECT count(*) FROM ("  + allPendingVerificationIr + " ) as total";
    String countAllPendingApprovalIr = " SELECT count(*) FROM ("  + allPendingApprovalIr + " ) as total";
    String countAllVerifiedOrApprovedIr = " SELECT count(*) FROM ("  + allVerifiedOrApprovedIr + " ) as total";
    String countAllProcessedIr = " SELECT count(*) FROM ("  + allProcessedIr + " ) as total";


    @Query(value = allIr, countQuery = countAllIr, nativeQuery = true)
    Page<IrListInfo> findAllIr(LocalDateTime fromDate, LocalDateTime toDate,Pageable pageable);



    @Query("select max(ir.id) from InternalRequisition ir")
    Optional<Long> findMaxOrderById();

    @Query(value = allClosedIr, countQuery = countAllIr, nativeQuery = true)
    Page<IrListInfo> findAllClosedIr(LocalDateTime fromDate, LocalDateTime toDate,Pageable pageable);

    @Query(value = allPendingVerificationIr, countQuery = countAllPendingVerificationIr, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingVerificationIr(@Param("nextVerifierId") String nextVerifierId,
                                                          LocalDateTime fromDate,LocalDateTime toDate,
                                                          Pageable pageable);

    @Query(value = allPendingApprovalIr, countQuery = countAllPendingApprovalIr, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingApprovalIr(@Param("nextApproverId") String nextApproverId,
                                                      LocalDateTime fromDate, LocalDateTime toDate,
                                                      Pageable pageable);

    @Query(value = allVerifiedOrApprovedIr, countQuery = countAllVerifiedOrApprovedIr, nativeQuery = true)
    Page<IrListInfo> findAllVerifiedOrApprovedIr(LocalDateTime fromDate,
                                                 LocalDateTime toDate, Pageable pageable);

    @Query(value = allProcessedIr, countQuery = countAllProcessedIr, nativeQuery = true)
    Page<IrListInfo> findAllProcessedIr(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    <T> Optional<T> findById(Long id, Class<T> t);

    interface IrDetail{
        Long getId();
        String getInternalRequisitionNo();
        Employee getRequestedBy();
        CategoryInfo getCategory();
        WarehouseInfo getWarehouse();
        String getPriority();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getDeliveryDate();
        List<InternalRequisitionDetail> getDetails();
    }

    interface CategoryInfo {
        Long getId();
        String getCode();
        String getName();
    }

    interface WarehouseInfo {

        Long getId();
        String getName();
        String getLocation();
    }

    interface IrVerifierListInfo extends IrListInfo{
        String getEmployeeName();
    }
    interface IrListInfo{
        Long getId();
        String getIrNo();
        String getCategoryName();
        Integer getItemsQty();
        String getStatus();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getDeliveryDate();
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime getCreatedAt();
        String getWarehouse();
    }
}
