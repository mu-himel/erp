package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RemoteLedgerAccountDto {
    private String itemCode;
    private String itemName;
    private String itemGroup;
    private String itemSubGroup;
    private String uom;
    private BigDecimal openingDebit;
    private BigDecimal openingCredit;
    private String warehouse;
}
