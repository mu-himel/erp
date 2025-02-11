package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CounterItemDto {
    private String productDescription;
    private String specification;
    private Long estimatedDeliveryDays;
    private BigDecimal itemQuantity;
    private String brandName;
    private CounterPriceQuotation priceQuotation;
    private Integer warrantyDuration;
    private String warrantyUnit;
}
