package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DeliveryDetailDto {
    private Long warehouseId;
    private String deliveryChargeMode;
    private BigDecimal deliveryChargeAmount;
    private List<ItemDeliveryDetailDto> items;
}
