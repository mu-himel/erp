package com.agi.aesl.erpscm.inventory.dto.request;


import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.*;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;

import jakarta.validation.constraints.NotBlank;
// import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto implements EntityConvertable<Item> {

    private Long id;

    @NotBlank(message = "name is required")
    // @ApiModelProperty(required = true)
    private String name;

    // @ApiModelProperty(required = true)
    private String code;

    private String sku;

    private Long orgId;

    private ItemCategory itemCategory;
    private ItemCategory itemParentCategory;

    private String itemUnit;

    private List<ItemFunctionalUnit> functionalUnits = new ArrayList<>();

    private Integer stockThresholdQty;

    private BigDecimal currentStockQty;

    private BigDecimal reorderPercentage;

    private List<ItemAttribute> attributes;



    private ReferenceObjectDto brand;

    private ReferenceObjectDto warehouse;

    private ReferenceObjectDto warehouseStore;

    private Boolean active;
    private Long scmItemId;

    private String employee;

    @Override
    public Item getEntity() {
        Item item = new Item(id);
        BeanUtils.copyProperties(this,item);
        return item;
    }
}
