package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.Data;

@Data
public class RemoteLedgerAccDto {
//    private LedgerInitiatorDto initiatorDetailsDto;
    private String atrName;
    private String brandName;
    private String itemCode;
    private String categoryCode;
    private Long itemId;
    private Long categoryId;
    private Long warehouseId;
    private String category;
    private Long subCategoryId;
    private String subCategory;
    private String subCategoryCode;

}
