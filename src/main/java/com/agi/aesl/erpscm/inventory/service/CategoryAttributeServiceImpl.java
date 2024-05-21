package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.repository.CategoryAttributeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryAttributeServiceImpl implements CategoryAttributeService{

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Override
    public List<CategoryAttribute> getAttributesByCategory(Long categoryId) {
        return categoryAttributeRepository.findAllByCategoryId(categoryId);
    }
}
