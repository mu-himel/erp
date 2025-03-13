package com.agi.aesl.erpscm.demand.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.agi.aesl.erpscm.demand.entity.DemandDetailAttribute;
import com.agi.aesl.erpscm.demand.enums.DemandPriority;
import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandDetailItemResDto {
    Long itemId;
    Long itemCategoryId;
    Long itemParentCategoryId;
    List<DemandDetailAttribute> attributes;
    String name;
    Long brandId;
    String brandName;
    String category;
    String parentCategory;
    String parentCategoryCode;
    String categoryCode;

    String code;
    String attributeTypes;
    String attributeValues;
    String receivedNote;
    String declineNote;
    String storeNote;
    Long demandDetailId;
    Long prQty;
    Long openPrQty;
    DemandStatus demandItemStatus;
    String specification;
    DemandPriority demandPriority;
    BigDecimal approvedQuantity;
    BigDecimal requestedQuantity;
    BigDecimal currentStock;
    BigDecimal stockThresholdQty;
    String itemUnit;
    BigDecimal inTransit;
}
