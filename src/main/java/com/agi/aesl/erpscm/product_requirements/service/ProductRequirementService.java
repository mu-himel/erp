package com.agi.aesl.erpscm.product_requirements.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.product_requirements.dto.response.PrItemInfo;
import com.agi.aesl.erpscm.product_requirements.repository.ProductRequirementRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;
import com.agi.aesl.erpscm.product_requirements.enums.ProductRequirementStatus;

public interface ProductRequirementService {
    void createProductRequirement(Jwt token, ProductRequirementRequestDto productRequirementRequestDto);

    Page<ProductRequirementRepository.ProductRequirementInfo> getAllProductRequirements(
        Jwt token,
        Optional<Integer> page, 
        Optional<Integer> size, 
        Optional<Long> categoryId,
        Optional<Long> subCategoryId, 
        Optional<String> startDate,
        Optional<String> endDate,
        Optional<Integer> daysRemain
        );

    List<PrItemInfo> getAllProductRequirementView(Optional<Long> categoryId, Optional<Long> subCategoryId);

    int updateStatusByCategoryAndSubCategory(
        ProductRequirementStatus toStatus,
        ProductRequirementStatus fromStatus,
        Long categoryId,
        Long subCategoryId
    );

    List<ProductRequirementRepository.WarehouseRequirement> getWarehouseRequirements(String attribute);

    void reOpen(String productRequirementsIds);


    List<ProductRequirementRepository.PrDemandView> getDemandByProductRequirementIds(String prIds);
}
