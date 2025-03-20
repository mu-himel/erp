package com.agi.aesl.erpscm.product_requirements.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agi.aesl.erpscm.common.BaseController;
import com.agi.aesl.erpscm.product_requirements.dto.request.ProductRequirementRequestDto;
import com.agi.aesl.erpscm.product_requirements.dto.request.WarehouseRequirementDto;
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
    public ResponseEntity<Void> addProductRequirement(
        @AuthenticationPrincipal Jwt token,
        @RequestBody @Valid ProductRequirementRequestDto productRequirementRequestDto
    ){
        productRequirementService.createProductRequirement(token, productRequirementRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Object> getProductRequirement(
        @AuthenticationPrincipal Jwt token,
        @RequestParam("page") Optional<Integer> page,
        @RequestParam("size") Optional<Integer> size,
        @RequestParam("categoryId") Optional<Long> categoryId,
        @RequestParam("subCategoryId") Optional<Long> subCategoryId,
        @RequestParam("fromDate") Optional<String> startDate,
        @RequestParam("toDate") Optional<String> endDate,
        @RequestParam("daysRemain") Optional<Integer> daysRemain
    ){
        return new ResponseEntity<>(
            productRequirementService.getAllProductRequirements(token, page,size,categoryId,subCategoryId,startDate,endDate,
                    daysRemain),
            HttpStatus.OK
        );
    }

    @GetMapping("/{categoryId}/{subCategoryId}")
    public ResponseEntity<Object> getProductRequirementView(
        @PathVariable("categoryId") Optional<Long> categoryId,
        @PathVariable("subCategoryId") Optional<Long> subCategoryId
    ){
        return new ResponseEntity<>(
            productRequirementService.getAllProductRequirementView(categoryId,subCategoryId),
            HttpStatus.OK
        );
    }

    @PostMapping("/warehouse-requirements")
    public ResponseEntity<Object> getWarehouseRequirements(
        @RequestBody WarehouseRequirementDto dto
    ){
        return new ResponseEntity<>(
            productRequirementService.getWarehouseRequirements(dto.getAttribute()),
            HttpStatus.OK
        );
    }

    @GetMapping("/demand-by-ids")
    public ResponseEntity<Object> getDemandInfoByIds(
            @RequestParam("prIds") String prIds
    ) {
        return new ResponseEntity<>(
                productRequirementService.getDemandByProductRequirementIds(prIds),
                HttpStatus.OK
        );
    }

}
