package com.agi.aesl.erpscm.product_requirements.service;

import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;

import jakarta.validation.Valid;

public interface ProductRequirementService {
    void createProductRequirement(Jwt token, ProductRequirementRequestDto productRequirementRequestDto);
}
