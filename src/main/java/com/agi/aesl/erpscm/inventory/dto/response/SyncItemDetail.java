package com.agi.aesl.erpscm.inventory.dto.response;

import java.util.List;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncItemDetail implements EntityConvertable<Item>{

    private Long id;
    private String code;
    private String name; 
    private Boolean isSyncronized;
    private String sku;
    private String itemUnit;
    private String manufacturer;
    private BrandInfo brand;
    private StoreTypeInfo storeType;
    private List<ItemFunctionalUnit>functionalUnits;
    private List<ItemAttribute> attributes;
    private SubCateInfo itemCategory;

    @Override
    public Item getEntity() {
        Item item = new Item();
        item.setCode(this.code);
        return item;
    }
     
    public record BrandInfo(Long id, String name) {}

    public record StoreTypeInfo(Long id, String name, Boolean active, Boolean isDefault) {}

    public record SubCateInfo(Long id, String code, String name, List<CategoryAttribute> attributes){}
}



