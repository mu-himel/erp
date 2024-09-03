package com.agi.aesl.erpscm.quality_control.repository;

public interface QcQuery {

    String qcResultByGrnId= """
            SELECT qc.id as id, qc.qc_status as qcStatus,qc.comment as comment, 
                    qck.name as name,
                    qck.remark as remark,
                    qc.review_prev_status as prevStatus,
                    qc.reviewer_id as reviewerId,
                CASE WHEN qck.qc_type = 'PASS' THEN
                 true
                END as pass,
                CASE WHEN qck.qc_type = 'HOLD' THEN
                 true
                END as hold,
                CASE WHEN qck.qc_type = 'FAIL' THEN
                 true
                END as fail
                FROM quality_controls qc
                LEFT JOIN quality_control_kpis qck ON qck.quality_control_id = qc.id
                WHERE qc.good_receive_note_id = :id
            """;

    String getPendingVerificationsQc = """
            SELECT 
                    p.id as id,
                    p.qcId as qcId,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.qcStatus as qcStatus,
                    p.grnMode as grnMode,
                    p.indentNo as indentNo,
                    p.grnNo as grnNo,
                    p.categoryName as categoryName,
                    p.items as items,
                    p.receivedQty as receivedQty,
                    p.qcPending as qcPending,
                    p.qcPass as qcPass,
                    p.qcFail as qcFail,
                    p.qcHold as qcHold,
                    p.warehouseId as warehouseId
                FROM (
                    SELECT      
                                grn.id as id,
                                qc.id as qcId,
                                grn.created_at as createdAt, 
                                grn.grn_no as grnNo, 
                                grn.grn_status as grnStatus,
                                CASE WHEN  qc.qc_status != 'REVIEW'  AND (qvah.id IS NOT NULL AND qvah.employee_id = :nextVerifierId) THEN
                                    qvah.qc_status
                                ELSE
                                    qc.qc_status
                                END as  qcStatus,
                                grn.indent_no as indentNo,
                                ic.name as categoryName, 
                                count(grid.id) as items, sum(grid.receive_qty) as receivedQty,
                                grn.grn_mode as grnMode,
                                null as po,
                                CASE WHEN grid.qc_type IS NULL THEN
                                    coalesce(count(grid.qc_type),0)
                                END qcPending,
                                COALESCE(CASE WHEN grid.qc_type = 'PASS' THEN
                                    count(grid.qc_type)
                                END,0) qcPass,
                                COALESCE(CASE WHEN grid.qc_type = 'FAIL' THEN
                                    count(grid.qc_type)
                                END,0) qcFail,
                                COALESCE(CASE WHEN grid.qc_type = 'HOLD' THEN
                                    count(grid.qc_type)
                                END,0) qcHold,
                                grn.warehouse_id as warehouseId 
                    FROM quality_controls qc
                    LEFT JOIN qc_verify_approval_histories qvah ON qvah.quality_control_id = qc.id
                    LEFT JOIN good_receive_notes grn ON grn.id = qc.good_receive_note_id
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ipc ON ipc.id = grid.category_id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.sub_category_id
                    WHERE ((qc.next_verifier_id=:nextVerifierId AND qc.qc_status IN (:status))
                    OR (qvah.employee_id = :nextVerifierId AND qvah.qc_status = 'VERIFIED'))
                    AND (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds)) 
                            OR 
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                    AND (:fromDate IS NULL OR (qc.created_at BETWEEN :fromDate AND :toDate))
                    GROUP BY qc.id
                    ) p
            WHERE (:qty IS NULL OR p.items = :qty)
            AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)  
            """;

    String countPendingVerifications = "SELECT COUNT(*) FROM ("+getPendingVerificationsQc+") as total";


    String getPendingApprovalsQc = """
            SELECT 
                    p.id as id,
                    p.qcId as qcId,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.qcStatus as qcStatus,
                    p.indentNo as indentNo,
                    p.grnNo as grnNo,
                    p.categoryName as categoryName,
                    p.items as items,
                    p.receivedQty as receivedQty,
                    p.qcPending as qcPending,
                    p.qcPass as qcPass,
                    p.qcFail as qcFail,
                    p.qcHold as qcHold,
                    p.warehouseId as warehouseId
                FROM (
                    SELECT      grn.id as id,
                                qc.id as qcId,
                                grn.created_at as createdAt, 
                                grn.grn_no as grnNo, 
                                grn.grn_status grnStatus,
                                CASE WHEN qc.qc_status != 'REVIEW' AND (qvah.id IS NOT NULL AND qvah.employee_id = :nextApproveId) THEN
                                    qvah.qc_status
                                ELSE
                                    qc.qc_status
                                END as  qcStatus,
                                grn.indent_no as indentNo,
                                ic.name as categoryName, 
                                count(grid.id) as items, sum(grid.receive_qty) as receivedQty,
                                grn.grn_mode as grnMode,
                                null as po,
                                CASE WHEN grid.qc_type IS NULL THEN
                                    coalesce(count(grid.qc_type),0)
                                END qcPending,
                                COALESCE(CASE WHEN grid.qc_type = 'PASS' THEN
                                    count(grid.qc_type)
                                END,0) qcPass,
                                COALESCE(CASE WHEN grid.qc_type = 'FAIL' THEN
                                    count(grid.qc_type)
                                END,0) qcFail,
                                COALESCE(CASE WHEN grid.qc_type = 'HOLD' THEN
                                    count(grid.qc_type)
                                END,0) qcHold,
                                grn.warehouse_id as warehouseId 
                    FROM quality_controls qc
                     LEFT JOIN qc_verify_approval_histories qvah ON qvah.quality_control_id = qc.id
                    LEFT JOIN good_receive_notes grn ON grn.id = qc.good_receive_note_id
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ipc ON ipc.id = grid.category_id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.sub_category_id
                    WHERE ((qc.next_approver_id=:nextApproveId AND qc.qc_status IN (:status))
                    OR (qvah.employee_id = :nextApproveId AND qvah.qc_status = 'APPROVED'))
                    AND (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds)) 
                            OR 
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                    AND (:fromDate IS NULL OR (qc.created_at BETWEEN :fromDate AND :toDate))
                    GROUP BY qc.id
                    ) p
            WHERE (:qty IS NULL OR p.items = :qty)
            AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)  
            """;

    String countPendingApprovals = "SELECT COUNT(*) FROM ("+getPendingApprovalsQc+") as total";

    String getClosedQc = """
            SELECT 
                    p.id as id,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.indentNo as indentNo,
                    p.grnNo as grnNo,
                    p.categoryName as categoryName,
                    p.items as items,
                    p.receivedQty as receivedQty,
                    p.qcPending as qcPending,
                    p.qcPass as qcPass,
                    p.qcFail as qcFail,
                    p.qcHold as qcHold,
                    p.warehouseId as warehouseId
                FROM (
                    SELECT      qc.id as id,
                                grn.created_at as createdAt, 
                                grn.grn_no as grnNo, 
                                grn.grn_status grnStatus,
                                grn.indent_no as indentNo,
                                ic.name as categoryName, 
                                count(grid.id) as items, sum(grid.receive_qty) as receivedQty,
                                grn.grn_mode as grnMode,
                                null as po,
                                CASE WHEN grid.qc_type IS NULL THEN
                                    coalesce(count(grid.qc_type),0)
                                END qcPending,
                                COALESCE(CASE WHEN grid.qc_type = 'PASS' THEN
                                    count(grid.qc_type)
                                END,0) qcPass,
                                COALESCE(CASE WHEN grid.qc_type = 'FAIL' THEN
                                    count(grid.qc_type)
                                END,0) qcFail,
                                COALESCE(CASE WHEN grid.qc_type = 'HOLD' THEN
                                    count(grid.qc_type)
                                END,0) qcHold,
                                grn.warehouse_id as warehouseId 
                    FROM quality_controls qc
                    LEFT JOIN good_receive_notes grn ON grn.id = qc.good_receive_note_id
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ipc ON ipc.id = grid.category_id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.sub_category_id
                    WHERE (qc.qc_status IN ('REJECTED','APPROVED','VERIFIED','COMPLETED'))
                    AND (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds)) 
                            OR 
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                    AND (:fromDate IS NULL OR (qc.created_at BETWEEN :fromDate AND :toDate))
                    GROUP BY qc.id
                    ) p
            WHERE (:qty IS NULL OR p.items = :qty)
            AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)  
            """;

    String countClosed = "SELECT COUNT(*) FROM ("+getClosedQc+") as total";

    String getRejectedQc = """
            SELECT 
                    p.id as id,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.indentNo as indentNo,
                    p.grnNo as grnNo,
                    p.categoryName as categoryName,
                    p.items as items,
                    p.receivedQty as receivedQty,
                    p.qcPending as qcPending,
                    p.qcPass as qcPass,
                    p.qcFail as qcFail,
                    p.qcHold as qcHold,
                    p.warehouseId as warehouseId
                FROM (
                    SELECT      qc.id as id,
                                grn.created_at as createdAt, 
                                grn.grn_no as grnNo, 
                                grn.grn_status grnStatus,
                                grn.indent_no as indentNo,
                                ic.name as categoryName, 
                                count(grid.id) as items, sum(grid.receive_qty) as receivedQty,
                                grn.grn_mode as grnMode,
                                null as po,
                                CASE WHEN grid.qc_type IS NULL THEN
                                    coalesce(count(grid.qc_type),0)
                                END qcPending,
                                COALESCE(CASE WHEN grid.qc_type = 'PASS' THEN
                                    count(grid.qc_type)
                                END,0) qcPass,
                                COALESCE(CASE WHEN grid.qc_type = 'FAIL' THEN
                                    count(grid.qc_type)
                                END,0) qcFail,
                                COALESCE(CASE WHEN grid.qc_type = 'HOLD' THEN
                                    count(grid.qc_type)
                                END,0) qcHold,
                                grn.warehouse_id as warehouseId 
                    FROM quality_controls qc
                    LEFT JOIN good_receive_notes grn ON grn.id = qc.good_receive_note_id
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ipc ON ipc.id = grid.category_id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.sub_category_id
                    WHERE (qc.qc_status IN ('REJECTED'))
                    AND (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds)) 
                            OR 
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                    AND (:fromDate IS NULL OR (qc.created_at BETWEEN :fromDate AND :toDate))
                    GROUP BY qc.id
                    ) p
            WHERE (:qty IS NULL OR p.items = :qty)
            AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)  
            """;

    String countRejected = "SELECT COUNT(*) FROM ("+getRejectedQc+") as total";

    interface QcResultItem{
        Long getId();
        String getQcStatus();
        String getPrevStatus();
        String getReviewerId();
        String getName();
        String getRemark();
        String getComment();
        Long getPass();
        Long getFail();
        Long getHold();
    }
}
