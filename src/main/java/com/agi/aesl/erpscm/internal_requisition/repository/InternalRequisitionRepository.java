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
            ir.created_at as createdAt
        FROM internal_requisitions ir 
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING','PENDING_VERIFICATION','PENDING_APPROVAL', 'REVIEW','PROCESSING')
        GROUP BY ir.id
        """;

    String allClosedIr = """
            SELECT 
                ir.id as id,
                ir.internal_requisition_no as irNo,
                c.name as categoryName,
                count(ird.id) as itemsQty,
                ir.ir_status as status,
                ir.created_at as createdAt
            FROM internal_requisitions ir 
            LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
            LEFT JOIN scm_item_categories c ON c.id = ir.category_id
            WHERE ir.ir_status IN ('RECEIVED', 'REJECTED')
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
            ir.created_at as createdAt
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING_VERIFICATION', 'REVIEW','VERIFIED')
        AND ir.next_verifier_id = :nextVerifierId
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
            ir.created_at as createdAt
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('PENDING_APPROVAL', 'REVIEW','APPROVED')
        AND ir.next_approver_id = :nextApproverId
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
            ir.created_at as createdAt,
            w.name as warehouse
        FROM internal_requisitions ir 
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND (ir.is_processed IS NULL OR ir.is_processed=0)
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
            ir.created_at as createdAt,
            w.name as warehouse
        FROM internal_requisitions ir 
        LEFT JOIN warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
        GROUP BY ir.id
        """;

    String countAllIr = " SELECT count(*) FROM ("  + allIr + " )";
    String countAllClosedIr = " SELECT count(*) FROM ("  + allClosedIr + " )";
    String countAllPendingVerificationIr = " SELECT count(*) FROM ("  + allPendingVerificationIr + " )";
    String countAllPendingApprovalIr = " SELECT count(*) FROM ("  + allPendingApprovalIr + " )";
    String countAllVerifiedOrApprovedIr = " SELECT count(*) FROM ("  + allVerifiedOrApprovedIr + " )";
    String countAllProcessedIr = " SELECT count(*) FROM ("  + allProcessedIr + " )";


    @Query(value = allIr, countQuery = countAllIr, nativeQuery = true)
    Page<?> findAllIr(Pageable pageable);



    @Query("select max(ir.id) from InternalRequisition ir")
    Optional<Long> findMaxOrderById();

    @Query(value = allClosedIr, countQuery = countAllIr, nativeQuery = true)
    Page<IrListInfo> findAllClosedIr(Pageable pageable);

    @Query(value = allPendingVerificationIr, countQuery = countAllPendingVerificationIr, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingVerificationIr(@Param("nextVerifierId") String nextVerifierId,
                                                          Pageable pageable);

    @Query(value = allPendingApprovalIr, countQuery = countAllPendingApprovalIr, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingApprovalIr(@Param("nextApproverId") String nextApproverId, Pageable pageable);

    @Query(value = allVerifiedOrApprovedIr, countQuery = countAllVerifiedOrApprovedIr, nativeQuery = true)
    Page<IrListInfo> findAllVerifiedOrApprovedIr(Pageable pageable);

    @Query(value = allProcessedIr, countQuery = countAllProcessedIr, nativeQuery = true)
    Page<IrListInfo> findAllProcessedIr(Pageable pageable);

    <T> Optional<T> findById(Long id, Class<T> t);

    interface IrDetail{
        Long getId();
        String getInternalRequisitionNo();
        Employee getRequestedBy();
        CategoryInfo getCategory();
        WarehouseInfo getWarehouse();
        String getPriority();
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
        LocalDateTime getCreatedAt();
        String getWarehouse();
    }
}
