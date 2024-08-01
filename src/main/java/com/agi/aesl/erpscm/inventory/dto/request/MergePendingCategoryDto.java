package com.agi.aesl.erpscm.inventory.dto.request;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.organization.entity.Organization;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MergePendingCategoryDto {
    private String name;
    private String code;
    private ItemCategory parentCategory;
    private Long mergeCategoryId;
    private List<CategoryAttribute> attributes;
    private List<String> brands;
    private BigDecimal vat;
    private Organization organization;
}
