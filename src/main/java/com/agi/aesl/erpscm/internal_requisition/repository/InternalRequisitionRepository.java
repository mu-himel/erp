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
    String COUNT_START=" SELECT count(*) FROM (";
    String COUNT_END=") as TOTAL";
    String ALL_IR = """
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

    String ALL_CLOSED_IR = """
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
            WHERE ir.ir_status IN ('RECEIVED', 'REJECTED','COMPLETED')
            AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate))
            GROUP BY ir.id
            """;

    String ALL_PV_IR = """
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

    String ALL_PA_IR = """
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

    String ALL_VA_IR = """
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

    String ALL_PROCESSED_IR = """
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
        LEFT JOIN scm_warehouses w ON w.id = ir.warehouse_id
        LEFT JOIN acl_users e ON e.id = ir.requested_by_id
        LEFT JOIN internal_requisition_details ird ON ird.ir_id = ir.id
        LEFT JOIN scm_item_categories c ON c.id = ir.category_id
        WHERE ir.ir_status IN ('VERIFIED','APPROVED') AND ir.is_processed=1
        AND (COALESCE(:fromDate) IS NULL OR (ir.delivery_date BETWEEN :fromDate AND :toDate))
        GROUP BY ir.id
        """;

    String COUNT_ALL_IR =   COUNT_START + ALL_IR + COUNT_END;
    String COUNT_ALL_CLOSED_IR = COUNT_START  + ALL_CLOSED_IR + COUNT_END;
    String COUNT_ALL_PV_IR = COUNT_START  + ALL_PV_IR + COUNT_END;
    String COUNT_ALL_PA_IR = COUNT_START  + ALL_PA_IR + COUNT_END;
    String COUNT_ALL_VA_IR = COUNT_START  + ALL_VA_IR + COUNT_END;
    String COUNT_ALL_PROCESSED_IR = COUNT_START + ALL_PROCESSED_IR + COUNT_END;


    @Query(value = ALL_IR, countQuery = COUNT_ALL_IR, nativeQuery = true)
    Page<IrListInfo> findAllIr(LocalDateTime fromDate, LocalDateTime toDate,Pageable pageable);



    @Query("select max(ir.id) from InternalRequisition ir")
    Optional<Long> findMaxOrderById();

    @Query(value = ALL_CLOSED_IR, countQuery = COUNT_ALL_CLOSED_IR, nativeQuery = true)
    Page<IrListInfo> findAllClosedIr(LocalDateTime fromDate, LocalDateTime toDate,Pageable pageable);

    @Query(value = ALL_PV_IR, countQuery = COUNT_ALL_PV_IR, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingVerificationIr(@Param("nextVerifierId") String nextVerifierId,
                                                          LocalDateTime fromDate,LocalDateTime toDate,
                                                          Pageable pageable);

    @Query(value = ALL_PA_IR, countQuery = COUNT_ALL_PA_IR, nativeQuery = true)
    Page<IrVerifierListInfo> findAllPendingApprovalIr(@Param("nextApproverId") String nextApproverId,
                                                      LocalDateTime fromDate, LocalDateTime toDate,
                                                      Pageable pageable);

    @Query(value = ALL_VA_IR, countQuery = COUNT_ALL_VA_IR, nativeQuery = true)
    Page<IrListInfo> findAllVerifiedOrApprovedIr(LocalDateTime fromDate,
                                                 LocalDateTime toDate, Pageable pageable);

    @Query(value = ALL_PROCESSED_IR, countQuery = COUNT_ALL_PROCESSED_IR, nativeQuery = true)
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
