package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

@Data
public class CounterItemDto {
    private String productDescription;
    private String specification;
    private Long estimatedDeliveryDays;
    private Long itemQuantity;
    private String brandName;
    private CounterPriceQuotation priceQuotation;
    private Integer warrantyDuration;
    private String warrantyUnit;
}
