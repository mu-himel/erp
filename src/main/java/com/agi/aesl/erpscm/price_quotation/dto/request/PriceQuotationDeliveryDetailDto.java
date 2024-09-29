package com.agi.aesl.erpscm.price_quotation.dto.request;

import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceQuotationDeliveryDetailDto {
    private  String warehouseName;
    private Long warehouseId;
    private DeliveryCharge deliveryChargeType;
    private BigDecimal deliveryOrderQty;
    private BigDecimal deliveryChargeAmount;
}
