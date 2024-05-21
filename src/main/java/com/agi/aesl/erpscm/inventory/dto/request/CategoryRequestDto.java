package com.agi.aesl.erpscm.inventory.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto implements EntityConvertable<ItemCategory>{

    private Long id;

    @NotBlank(message = "Name is required")
    // @ApiModelProperty(required = true)
    private String name;

    // @ApiModelProperty(required = true)
    private String code;

    private BigDecimal currentYearBudget;
    private Optional<Long> budgetId;

    private ItemCategory parentCategory;

    private List<CategoryAttribute> attributes;
    private List<CategoryBrand> brands;



    private ReferenceObjectDto warehouse;
    private ReferenceObjectDto warehouseStore;

    private BigDecimal vat;

    private Long cpsCategoryId;

    private Long requestedBy;

    @Override
    public ItemCategory getEntity() {
        ItemCategory category = new ItemCategory(id);
        BeanUtils.copyProperties(this,category);
        return category;
    }
    
    
}
