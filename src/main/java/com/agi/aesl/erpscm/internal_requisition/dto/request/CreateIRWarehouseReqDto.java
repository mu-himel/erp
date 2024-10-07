package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateIRWarehouseReqDto {
    private Long id;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private BigDecimal qty;
    private BigDecimal currentStock;
    private BigDecimal safetyStock;
    private BigDecimal receiveQty;
}
