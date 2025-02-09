package com.agi.aesl.erpscm.rfq.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RfqItemDto {
    private Long id;
    private List<PrWarehouseQtyDto> warehouses;
    private BigDecimal orderQty;
    private Long subCategoryId;
    private String attribute;
    private String brandName;
    private Long brandId;
}
