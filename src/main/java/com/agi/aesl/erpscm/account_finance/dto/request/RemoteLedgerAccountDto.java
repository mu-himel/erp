package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.Data;

@Data
public class RemoteLedgerAccountDto {
    private String itemCode;
    private String itemName;
    private String itemGroup;
    private String itemSubGroup;
    private String uom;
    private String warehouse;
}
