package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import lombok.Data;

import java.util.List;

@Data
public class ItemMergeRequestDto {
    private Long mergeItemId;

    private String name;
    private String itemAttributeName;
    private String code;
    private ItemCategory itemCategory;
    private ItemCategory itemParentCategory;
    private String itemUnit;
    private List<ItemAttribute> attributes;
    private String brand;
}
