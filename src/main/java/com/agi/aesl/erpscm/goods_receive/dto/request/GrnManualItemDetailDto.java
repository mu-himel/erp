package com.agi.aesl.erpscm.goods_receive.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GrnManualItemDetailDto {
    private ReferenceObjectDto item;
    private String itemCode;
    private ItemCategory category;
    private ItemCategory subCategory;
    private Integer estDeliveryDays;
    private BigDecimal orderQty;
    private BigDecimal pricePerUnit;
}
