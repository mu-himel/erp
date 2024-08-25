package com.agi.aesl.erpscm.quality_control.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QcDetailDto {
    private Long id;
    private BigDecimal declaredQty;
    private BigDecimal inspectedQty;
    private BigDecimal totalApproveQty;
    private BigDecimal totalDeclineQty;
    private String approveComment;
    private String declineComment;
}
