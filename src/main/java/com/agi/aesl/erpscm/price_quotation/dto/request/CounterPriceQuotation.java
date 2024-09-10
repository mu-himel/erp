package com.agi.aesl.erpscm.price_quotation.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CounterPriceQuotation {
    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;
}
