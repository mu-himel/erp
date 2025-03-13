package com.agi.aesl.erpscm.inventory.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.inventory.enums.CategoryStatus;
import org.springframework.beans.BeanUtils;

import com.agi.aesl.erpscm.common.EntityConvertable;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
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
    private String name;

    private String code;
    private String prefix;

    private Long userCategoryId;


    private BigDecimal currentYearBudget;
    private Optional<Long> budgetId;

    private ItemCategory parentCategory;

    private List<CategoryAttribute> attributes;
    private List<String> brands;



    private ReferenceObjectDto warehouse;
    private ReferenceObjectDto warehouseStore;

    private BigDecimal vat;

    private Boolean isForCps;

    private Long scmCategoryId;

    private Long cpsCategoryId;

    private Long requestedBy;

    private String employee;

    private Boolean isActive=false;


    private CategoryStatus categoryStatus;

    @Override
    public ItemCategory getEntity() {
        ItemCategory category = new ItemCategory(id);
        BeanUtils.copyProperties(this,category);
        if(category.getName().trim().equals("")){
            throw new RuntimeException("Sorry! Name field should not blank");
        }
        category.setName(category.getName().trim());
        return category;
    }
    
    
}
