package com.agi.aesl.erpscm.cs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CsRequestDto {
    private Long indentId;
    private BigDecimal vatAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalPrice;
    private BigDecimal subTotalPrice;
    private LocalDate validityDate;
    private List<CsDetailReqDto> details;
}
