package com.agi.aesl.erpscm.quality_control.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.quality_control.entity.QualityControlKpi;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import lombok.Data;

import java.util.List;

@Data
public class QcDto {
    private String comment;
    private ReferenceObjectDto grn;
    private List<QualityControlKpi> kpis;
    private QcStatus qcStatus;
    private List<QcDetailDto> qcItemDetails;

    public void setQcStatus(String qcStatus) {
        this.qcStatus = QcStatus.valueOf(qcStatus);
    }
}
