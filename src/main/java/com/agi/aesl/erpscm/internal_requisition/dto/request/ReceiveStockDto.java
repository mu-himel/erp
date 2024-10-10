package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceiveStockDto {
    private Long id;
    private Long itemId;
    private BigDecimal receiveQty;
    private Long warehouseId;
    private String note;
}
