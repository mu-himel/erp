package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDeliveryDetailDto {
    private String itemName;
    private BigDecimal deliveryOrderQty;
}
