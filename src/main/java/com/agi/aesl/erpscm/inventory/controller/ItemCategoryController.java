package com.agi.aesl.erpscm.inventory.controller;


import com.agi.aesl.erpscm.common.BaseController;
// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;
import com.agi.aesl.erpscm.inventory.dto.request.BulkCategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.service.CategoryService;

import jakarta.validation.Valid;

// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/item-categories")
public class ItemCategoryController extends BaseController{

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    // @ApiOperation(value = "Get Parent Categories With Pagination")
    public ResponseEntity<?> getParentItemCategories(@RequestParam("page") Optional<Integer> page,
                                               @RequestParam("size") Optional<Integer> size,
                                               @RequestParam("name")  Optional<String> name,
                                               @RequestParam("code") Optional<String> code,
                                               @RequestParam("currentYearBudget") Optional<BigDecimal> currentYearBudget,
                                               @RequestParam("productCount") Optional<Long> productCount,
                                               @RequestParam("warehouseId") Optional<Long> warehouseId,
                                               @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){
        return new ResponseEntity<>(
                categoryService.getItemCategories(page,size, name, code,currentYearBudget,productCount,
                        warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/sub-categories")
    // @ApiOperation(value = "Get Sub Categories With Pagination")
    public ResponseEntity<?> getSubItemCategories(
                     @RequestParam("page") Optional<Integer> page,
                     @RequestParam("size") Optional<Integer> size,
                     @RequestParam("name")  Optional<String> name,
                     @RequestParam("code") Optional<String> code,
                     @RequestParam("currentYearBudget") Optional<BigDecimal> currentYearBudget,
                     @RequestParam("productCount") Optional<Long> productCount,
                     @RequestParam("categoryId") Optional<Long> categoryId,
                     @RequestParam("warehouseId") Optional<Long> warehouseId,
                     @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId

    ){
        return new ResponseEntity<>(
                categoryService.getItemCategories(page,size, name, code,currentYearBudget,productCount,categoryId,
                        warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/main-categories")
    public ResponseEntity<?> getMainCategoryListForInventoryControl(
            @RequestParam("warehouse") Optional<Long> warehouseId,
            @RequestParam("warehouseStore") Optional<Long> warehouseStoreId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getCategoriesForInventoryControl(warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/main-categories")
    public ResponseEntity<?> getMainCategoryList(
                                             @RequestParam("warehouseId") Optional<Long> warehouseId,
                                             @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId,
                                             @RequestParam("name")  Optional<String> name,
                                             @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getCategories(warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getCategoryList(@RequestParam("storeId") Optional<Long> storeId,
                                            @RequestParam("categoryId")  Optional<Long> categoryId,
                                             @RequestParam("name")  Optional<String> name,
                                                @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategories(storeId,categoryId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCategoryList(@RequestParam("categoryId")  Optional<Long> categoryId,
                                             @RequestParam("name")  Optional<String> name,
                                             @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategoriesAll(categoryId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/list")
    public ResponseEntity<?> getSubCategoryListForInventoryControl(
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("storeId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategoriesForInventoryControl(categoryId,warehouseId,storeId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/list/pending")
    public ResponseEntity<?> getPendingSubCategoryListForInventoryControl(
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("storeId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getPendingSubCategoriesForInventoryControl(categoryId,warehouseId,storeId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    // @ApiOperation(value = "Get Category Detail By ID")
    public ResponseEntity<?> getItemCategory(
        // @ApiParam(value = "Category Id",example = "1", required = true) 
        @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getItemCategory(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending/{id}")
    // @ApiOperation(value = "Get Category Detail By ID")
    public ResponseEntity<?> getPendingItemCategory(
            // @ApiParam(value = "Category Id",example = "1", required = true)
            @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getPendingItemCategory(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/{id}")
    // @ApiOperation(value = "Get Category Detail By ID")
    public ResponseEntity<?> getItemCategoryForInventoryControl(
        // @ApiParam(value = "Category Id",example = "1", required = true) 
        @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getItemCategoryForInventoryControl(id),
                HttpStatus.OK
        );
    }


    @PostMapping("/bulk-create")
    // @ApiOperation(value = "Create multiple categories")
    public ResponseEntity<?> createCategories(@RequestBody BulkCategoryRequestDto categoryRequestDto){
        categoryService.addCategories(categoryRequestDto.getCategories());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<?> createItemCategory(
        @AuthenticationPrincipal Jwt loggedInUser,
        @RequestBody @Valid CategoryRequestDto categoryRequestDto){
        categoryService.addCategory(loggedInUser, categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItemCategory(
        @PathVariable("id") Long id,
        @RequestBody CategoryRequestDto categoryRequestDto){
        categoryService.updateCategory(id,categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItemCategory(
            // @ApiParam(value = "Category Id",example = "1", required = true) 
            @PathVariable("id") Long id,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("storeId") Long warehouseStoreId
    ){
        categoryService.deleteCategory(id,warehouseId,warehouseStoreId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<?> activeItemCategory(
        @PathVariable("id") Long id,
        @RequestParam("warehouseId") Long warehouseId,
        @RequestParam("storeId") Long warehouseStoreId
    ){
        categoryService.activeCategory(id, warehouseId, warehouseStoreId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/inventory-control/main-categories/pending")
    public ResponseEntity<?> getPendingCategories(
            Optional<Long> warehouseId,
            Optional<Long> warehouseStoreId,
            Optional<String> name,
            Optional<String> code
    ){
        return new ResponseEntity<>(
                categoryService.getPendingCategories(warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{categoryId}/{attributeId}")
    public ResponseEntity<?> deleteCategoryAttribute(
            // @ApiParam(value = "Category Id", example = "1", required = true) 
            @PathVariable("categoryId") Long categoryId,
            // @ApiParam(value = "Attribute Id", example = "1", required = true) 
            @PathVariable("attributeId") Long attributeId

    ){
        categoryService.deleteAttribute(categoryId,attributeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @GetMapping("/next-id")
    // @ApiOperation(value = "Get New Category Id")
    public ResponseEntity<?> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",categoryService.getNewCategoryCode());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }
}
