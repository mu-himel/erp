package com.agi.aesl.erpscm.pr_indent.dto.reqeust;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndentDetailInfo {
    private Long id; 
    private BigDecimal orderQty;

    private List<PartialDeliveryTimeDto> partialDeliveries;
}
