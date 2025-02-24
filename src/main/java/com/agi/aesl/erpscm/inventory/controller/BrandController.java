package com.agi.aesl.erpscm.inventory.controller;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryBrandDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.service.CategoryBrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController extends BaseController {

    @Autowired
    private CategoryBrandService categoryBrandService;

    @PostMapping
    public ResponseEntity<?> saveBrands(
            @AuthenticationPrincipal Jwt token,
            @RequestBody @Valid List<CategoryBrandDto> brands){
        return new ResponseEntity<>(categoryBrandService.createBrands(token,brands),
                HttpStatus.CREATED);
    }
}
