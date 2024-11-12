package com.agi.aesl.erpscm.purchase_order.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PoRemoteDetailReqDto {
    private String itemName;
    private BigDecimal itemQty;
    private BigDecimal deliveryCharge;
    private BigDecimal vatAmount;
    private BigDecimal vatPercent;
    private BigDecimal subTotal;
    private BigDecimal totalPrice;
    private List<PoRemoteDeliveryDetailDto> poDeliveryDetailsDtoList;
}
