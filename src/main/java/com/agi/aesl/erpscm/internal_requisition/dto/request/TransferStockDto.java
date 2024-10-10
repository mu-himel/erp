package com.agi.aesl.erpscm.internal_requisition.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferStockDto {
    private Long id;
    private Long itemId;
    private BigDecimal transferQty;
    private Long warehouseId;
}
