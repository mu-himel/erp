package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TenderItemDeliveryDetail {
    private Long warehouseId;
    private String wareHouseName;
    private String wareHouseAddress;
    private Long deliveryOrderQTY;
}
