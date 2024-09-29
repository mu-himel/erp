package com.agi.aesl.erpscm.cs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CsDeliveryDetailDto {
    private Long id;
    private Long warehouseId;
    private BigDecimal deliveryQty;
    private LocalDate deliveryDate;
}
