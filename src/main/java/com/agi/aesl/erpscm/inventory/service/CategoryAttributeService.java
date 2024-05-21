package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;

import java.util.List;

public interface CategoryAttributeService {

    List<CategoryAttribute> getAttributesByCategory(Long categoryId);
}
