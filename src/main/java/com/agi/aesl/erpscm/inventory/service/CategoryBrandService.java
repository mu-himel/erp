package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.inventory.dto.request.CategoryBrandDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface CategoryBrandService {
    List<CategoryBrand> createBrands(Jwt token, List<CategoryBrandDto> brands);
}
