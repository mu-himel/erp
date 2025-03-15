package com.agi.aesl.erpscm.product_requirements.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
public class PrItemInfo {
    String brandName;
    String categoryName;
    String subCategoryName;
    String itemName;
    String productRequirementIds;
    BigDecimal prQty;

    Long brandId;
    Long daysRemain;
    LocalDateTime priorityDate;

    private List<PrWarehouseInfo> warehouses=new ArrayList<>();

    public PrItemInfo(String productRequirementIds,String brandName, String categoryName, String subCategoryName, String itemName, BigDecimal prQty, List<PrWarehouseInfo> warehouses) {
        if(this.productRequirementIds!=null){
            this.productRequirementIds = this.getProductRequirementIds().concat(","+productRequirementIds);
        }else{
            this.productRequirementIds=productRequirementIds;
        }

        this.brandName = brandName;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.itemName = itemName;
        this.prQty = prQty;
        this.warehouses = warehouses;
    }

    public PrItemInfo(String productRequirementIds,String brandName, String categoryName, String subCategoryName, String itemName, BigDecimal prQty) {
        if(this.productRequirementIds!=null){
            this.productRequirementIds = this.getProductRequirementIds().concat(","+productRequirementIds);
        }else{
            this.productRequirementIds=productRequirementIds;
        }

        this.brandName = brandName;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
        this.itemName = itemName;
        this.prQty = prQty;
    }

    public void setProductRequirementIds(String productRequirementIds) {
        if(this.productRequirementIds!=null){
            this.productRequirementIds = this.getProductRequirementIds().concat(","+productRequirementIds);
        }else{
            this.productRequirementIds=productRequirementIds;
        }
    }

    public void setWarehouses(PrWarehouseInfo warehouse) {
        this.warehouses.add(warehouse);
    }
}
