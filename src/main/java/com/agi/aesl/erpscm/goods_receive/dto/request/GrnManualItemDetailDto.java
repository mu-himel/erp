package com.agi.aesl.erpscm.goods_receive.dto.request;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GrnManualItemDetailDto {
    private Long id;
    private ReferenceObjectDto item;
    private String itemCode;
    private ItemCategory category;
    private ItemCategory subCategory;
    private Integer estDeliveryDays;
    private BigDecimal orderQty;
    private BigDecimal pricePerUnit;
    private BigDecimal deliveryChargeAmount;
    private BigDecimal vatAmount;
    private LocalDate expireDate;
    private LocalDate productionDate;
}
