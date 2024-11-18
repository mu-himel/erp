package com.agi.aesl.erpscm.erpn_integration.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseRequestItem {
    private String creditDays;
    private String estDeliveryTime;
    private String itemCode;
    private BigDecimal qty;
    private BigDecimal vat;
    private BigDecimal deliveryCharge;
    private BigDecimal pricePerUnit;
    private String transactionType;
}
