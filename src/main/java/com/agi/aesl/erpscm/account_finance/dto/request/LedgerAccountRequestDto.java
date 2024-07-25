package com.agi.aesl.erpscm.account_finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerAccountRequestDto {
    private String groupAccount;
    private String masterAccount;
    private String subGroupAccount;
    private BigDecimal openingCreditAmount;
    private BigDecimal openingDebitAmount;
    private String openingDate;
    private Long categoryId;
    private Long subCategoryId;
}
