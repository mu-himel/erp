package com.agi.aesl.erpscm.product_requirements.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PrWarehouseInfo
{
    private String warehouseIds;
    private String warehouseName;
    private BigDecimal currentStock;

    private BigDecimal safetyStock;
    private BigDecimal prQty;
    private BigDecimal transitQty;
    private BigDecimal itemQty;
    public PrWarehouseInfo(String warehouseIds, String warehouseName, BigDecimal currentStock,
                           BigDecimal safetyStock, BigDecimal prQty, BigDecimal transitQty, BigDecimal itemQty){
        this.warehouseIds = warehouseIds;
        this.warehouseName = warehouseName;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
        this.prQty = prQty;
        this.transitQty = transitQty;
        this.itemQty = itemQty;
    }

}
