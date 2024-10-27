package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.Data;

@Data
public class RemoteLedgerAccDto {
    private LedgerInitiatorDto initiatorDetailsDto;
    private String atrName;
    private String brandName;
    private String itemCode;
    private String categoryCode;
    private String category;
    private String subCategory;
    private String subCategoryCode;

}
