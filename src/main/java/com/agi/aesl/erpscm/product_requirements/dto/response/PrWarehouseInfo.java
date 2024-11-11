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

    private Long safetyStock;
    private BigDecimal prQty;
    private Long transitQty;
    private BigDecimal itemQty;
    public PrWarehouseInfo(String warehouseIds, String warehouseName, BigDecimal currentStock,
                           Long safetyStock, BigDecimal prQty, Long transitQty, BigDecimal itemQty){
        this.warehouseIds = warehouseIds;
        this.warehouseName = warehouseName;
        this.currentStock = currentStock;
        this.safetyStock = safetyStock;
        this.prQty = prQty;
        this.transitQty = transitQty;
        this.itemQty = itemQty;
    }

}
