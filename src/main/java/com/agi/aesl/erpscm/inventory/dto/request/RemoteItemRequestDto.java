package com.agi.aesl.erpscm.inventory.dto.request;


import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.enums.ItemUnit;

import jakarta.validation.constraints.NotBlank;
// import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;



import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteItemRequestDto implements EntityConvertable<Item> {

    private Long id;

    @NotBlank(message = "name is required")
    // @ApiModelProperty(required = true)
    private String name;

    // @ApiModelProperty(required = true)
    private String code;

    private String sku;

    private String categoryCode;

    private ItemUnit itemUnit;

    private Integer stockThresholdQty;

    private BigDecimal currentStockQty;

    private Integer reorderPercentage;

    private List<ItemAttribute> attributes;

    private String brandName;

    private ReferenceObjectDto warehouse;

    private String storeType;

    @Override
    // @ApiModelProperty(hidden = true)
    public Item getEntity() {
        Item item = new Item(id);
        BeanUtils.copyProperties(this,item);
        return item;
    }
}
