package com.agi.aesl.erpscm.inventory.dto.response;

import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetail {
    Long id;
    String name;
    String code;
    Boolean active;
    ItemUnit itemUnit;
    ItemRepository.RefInfo brand;
    ItemRepository.RefInfo itemCategory;
    ItemRepository.RefInfo itemParentCategory;
    Integer stockThresholdQty;
    Integer reorderPercentage;
    List<ItemAttribute> attributes;
//    List<ItemStock> stocks;
    Map<String,List<Map<String,Object>>> warehouses;
}
