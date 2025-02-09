package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
public class TenderItemDeliveryDetail {
    private Long warehouseId;
    private String wareHouseName;
    private String wareHouseAddress;
    private BigDecimal deliveryOrderQTY;
}
