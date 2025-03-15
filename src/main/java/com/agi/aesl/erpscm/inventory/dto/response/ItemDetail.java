package com.agi.aesl.erpscm.inventory.dto.response;

import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetail {
    Long id;
    String name;
    String itemAttributeName;
    String code;
    Boolean active;
    String itemUnit;
    ItemRepository.RefInfo brand;
    ItemRepository.RefInfo itemCategory;
    ItemRepository.RefInfo itemParentCategory;
    BigDecimal stockThresholdQty;
    BigDecimal reorderPercentage;
    List<ItemAttribute> attributes;
    List<ItemFunctionalUnit> functionalUnits;
//    List<ItemStock> stocks;
    Map<String,List<Map<String,Object>>> warehouses;
}
