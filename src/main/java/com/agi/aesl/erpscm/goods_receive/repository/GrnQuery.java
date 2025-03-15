package com.agi.aesl.erpscm.goods_receive.repository;

public class GrnQuery {
    private GrnQuery(){}
    public static final String COUNT_START="SELECT COUNT(*) FROM (";
    public static final String COUNT_END=") AS TOTAL";

    public static final String GET_ALL_GRN = """
                SELECT
                    p.id as id,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.indentNo as indentNo,
                    p.grnMode as grnMode,
                    p.grnNo as grnNo,
                    p.categoryName as categoryName,
                    p.items as items,
                    p.receivedQty as receivedQty,
                    p.qcPending as qcPending,
                    p.qcPass as qcPass,
                    p.qcFail as qcFail,
                    p.qcHold as qcHold,
                    p.warehouseId as warehouseId,
                    p.poNo as poNo
                FROM (
                    SELECT
                        grn.id as id,
                        grn.created_at as createdAt,
                        grn.grn_status as grnStatus,
                        grn.grn_mode as grnMode,
                        grn.indent_no as indentNo,
                        grn.grn_no as grnNo, ic.name as categoryName,
                        count(grid.id) as items, sum(grid.receive_qty) as receivedQty,
                        CASE WHEN grid.qc_type IS NULL THEN
                            coalesce(count(grid.qc_type),0)
                        END qcPending,
                        COALESCE(CASE WHEN grid.qc_type = 'PASS' THEN
                            count(grid.qc_type)
                        END,0)qcPass,
                        COALESCE(CASE WHEN grid.qc_type = 'FAIL' THEN
                            count(grid.qc_type)
                        END,0) qcFail,
                        COALESCE(CASE WHEN grid.qc_type = 'HOLD' THEN
                            count(grid.qc_type)
                        END,0) qcHold,
                        grn.warehouse_id as warehouseId,
                        CASE WHEN grn.remote_po_id IS NOT NULL THEN
                            (SELECT po_no FROM purchase_orders po WHERE po.id=grn.remote_po_id)
                        ELSE
                            'MANUAL'
                        END as poNo
                    FROM good_receive_notes grn
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.category_id
                    WHERE (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (:status IS NULL OR grn.grn_status IN (:status))
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds))
                            OR
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                        AND (:fromDate IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                    GROUP BY grn.id
                ) p
                WHERE (:qty IS NULL OR p.items = :qty)
                AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)
            """;
    public static final String COUNT_ALL_GRN = COUNT_START+GET_ALL_GRN+COUNT_END;

    public static final String GET_ALL_GRN_BY_STATUS= """
            SELECT
                    p.id as id,
                    p.createdAt as createdAt,
                    p.grnStatus as grnStatus,
                    p.qcStatus as qcStatus,
                    p.grnMode as grnMode,
                    p.indentNo as indentNo,
                    p.grnNo as grnNo,
                    p.poNo as poNo,
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
                        grn.created_at as createdAt,
                        grn.grn_no as grnNo,
                        grn.po_no as poNo,
                        grn.grn_status as grnStatus,
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
                        grn.warehouse_id as warehouseId,
                        qc.qc_status as qcStatus
                    FROM good_receive_notes grn
                    LEFT JOIN quality_controls qc ON qc.good_receive_note_id = grn.id
                    LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                    LEFT JOIN scm_item_categories ipc ON ipc.id = grid.category_id
                    LEFT JOIN scm_item_categories ic ON ic.id = grid.sub_category_id
                    WHERE grn.grn_status IN (:status)
                    AND (:grnNo IS NULL OR grn.grn_no = :grnNo)
                    AND (:grnMode IS NULL OR grn.grn_mode = :grnMode)
                    AND (:poNo IS NULL OR grn.po_no = :poNo)
                    AND (COALESCE(:warehouseIds) IS NULL OR grn.warehouse_id IN (:warehouseIds))
                    AND (
                            ( COALESCE(:categoryIds) IS NULL OR grid.category_id IN (:categoryIds))
                            OR
                            ( COALESCE(:categoryIds) IS NULL OR grid.sub_category_id IN (:categoryIds))
                        )
                    AND (:fromDate IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                    GROUP BY grn.id
                ) p
            WHERE (:qty IS NULL OR p.items = :qty)
            AND (:receivedQty IS NULL OR p.receivedQty = :receivedQty)
            """;

    public static final String COUNT_ALL_GRN_BY_STATUS=COUNT_START+GET_ALL_GRN_BY_STATUS+COUNT_END;
}
