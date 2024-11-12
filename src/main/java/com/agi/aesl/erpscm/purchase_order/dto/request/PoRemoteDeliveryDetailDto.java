package com.agi.aesl.erpscm.purchase_order.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PoRemoteDeliveryDetailDto {

    private ReferenceObjectDto warehouse;
    private BigDecimal deliveryCharge;
    private BigDecimal itemQty;
}
