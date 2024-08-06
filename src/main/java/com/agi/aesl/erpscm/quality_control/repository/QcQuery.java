package com.agi.aesl.erpscm.quality_control.repository;

public interface QcQuery {

    String qcResultByGrnId= """
            SELECT qc.comment as comment, 
                    qck.name as name,
                    qck.remark as remark,
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

    interface QcResultItem{
        String getName();
        String getRemark();
        String getComment();
        Long getPass();
        Long getFail();
        Long getHold();
    }
}
