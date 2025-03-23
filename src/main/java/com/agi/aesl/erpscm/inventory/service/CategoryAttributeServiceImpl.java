package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.repository.CategoryAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryAttributeServiceImpl implements CategoryAttributeService{


    private final CategoryAttributeRepository categoryAttributeRepository;

    @Override
    public List<CategoryAttribute> getAttributesByCategory(Long categoryId) {
        return categoryAttributeRepository.findAllByCategoryId(categoryId);
    }
}
