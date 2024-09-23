package com.agi.aesl.erpscm.purchase_order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PoRemoteDetailReqDto {
    private String itemName;
    private BigDecimal itemQty;
}
