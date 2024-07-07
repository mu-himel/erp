package com.agi.aesl.erpscm.product_requirements.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;
import com.agi.aesl.erpscm.product_requirements.service.ProductRequirementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/product-requirements")
public class ProductRequirementController extends BaseController{

    private final ProductRequirementService productRequirementService;

    public ProductRequirementController(ProductRequirementService productRequirementService) {
        this.productRequirementService = productRequirementService;
    }

    @PostMapping
    public ResponseEntity<?> addProductRequirement(
        @AuthenticationPrincipal Jwt token,
        @RequestBody @Valid ProductRequirementRequestDto productRequirementRequestDto
    ){
        productRequirementService.createProductRequirement(token, productRequirementRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
