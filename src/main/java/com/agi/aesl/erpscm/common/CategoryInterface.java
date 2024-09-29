package com.agi.aesl.erpscm.common;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryInterface {
    Long getId();
    String getName();
    String getCode();
    CategoryInterface getParentCategory();
    List<CategoryAttributeInterface> getAttributeInterfaces();

    List<BrandInterface> getBrandInterfaces();

    BigDecimal getVat();

    void setCpsCategoryId(Long id);
}
