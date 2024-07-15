package com.agi.aesl.erpscm.goods_receive.repository;

public interface GrnQuery {

    String getAllGrn = """
                SELECT 
                    grn.id as id,
                    grn.created_at as createdAt, 
                    grn.grn_status grnStatus,
                    po.po_no as poNo,
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
                    END,0) qcHold
                FROM good_receive_notes grn
                LEFT JOIN purchase_orders po ON po.id = grn.purchase_order_id
                LEFT JOIN good_receive_item_details grid ON grid.good_receive_note_id = grn.id
                LEFT JOIN item_categories ic ON ic.id = grid.category_id
                WHERE (:fromDate IS NULL OR grn.created_at BETWEEN :fromDate AND :toDate)
                GROUP BY grn.id
            """;
    String countAllGrn = "SELECT count(*) FROM ("+getAllGrn+") as total";
}
