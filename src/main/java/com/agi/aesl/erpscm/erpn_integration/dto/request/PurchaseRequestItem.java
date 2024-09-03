package com.agi.aesl.erpscm.erpn_integration.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequestItem {
    private String itemCode;
    private BigDecimal acceptedQty;
    private BigDecimal rate;
    private String costCenter;
}
