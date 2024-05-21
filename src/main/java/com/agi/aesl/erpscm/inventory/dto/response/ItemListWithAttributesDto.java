package com.agi.aesl.erpscm.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemListWithAttributesDto {
    Long id;
    String name;
    String itemAttribute;
    String code;
    Long warehouseId;
    Long warehouseStoreId;
    Long brandId;
    String brandName;
    List<Map<String, Object>> attributes = new ArrayList<>();

}
