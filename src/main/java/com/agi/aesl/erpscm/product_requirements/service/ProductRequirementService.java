package com.agi.aesl.erpscm.product_requirements.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;

public interface ProductRequirementService {
    void createProductRequirement(Jwt token, ProductRequirementRequestDto productRequirementRequestDto);

    Page<?> getAllProductRequirements(
        Jwt token,
        Optional<Integer> page, 
        Optional<Integer> size, 
        Optional<Long> categoryId,
        Optional<Long> subCategoryId, 
        Optional<LocalDateTime> startDate, 
        Optional<LocalDateTime> endDate);

    List<?> getAllProductRequirementView(Optional<Long> categoryId, Optional<Long> subCategoryId);

    int updateStatusByCategoryAndSubCategory(
        ProductRequirementStatus toStatus,
        ProductRequirementStatus fromStatus,
        Long categoryId,
        Long subCategoryId
    );

    List<?> getWarehouseRequirements(String attribute);

    void reOpen(String productRequirementsIds);


    List<?> getDemandByProductRequirementIds(String prIds);
}
