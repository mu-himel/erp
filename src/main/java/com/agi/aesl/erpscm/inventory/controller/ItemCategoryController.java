package com.agi.aesl.erpscm.inventory.controller;


import com.agi.aesl.erpscm.common.BaseController;

import com.agi.aesl.erpscm.inventory.dto.request.BulkCategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryApproveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.service.CategoryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/item-categories")
@RequiredArgsConstructor
public class ItemCategoryController extends BaseController{


    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Object> getParentItemCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("page") Optional<Integer> page,
                                               @RequestParam("size") Optional<Integer> size,
                                               @RequestParam("name")  Optional<String> name,
                                               @RequestParam("code") Optional<String> code,
                                               @RequestParam("year") Optional<Integer> year,
                                               @RequestParam("currentYearBudget") Optional<BigDecimal> currentYearBudget,
                                               @RequestParam("productCount") Optional<Long> productCount,
                                               @RequestParam("warehouseId") Optional<Long> warehouseId,
                                               @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){
        return new ResponseEntity<>(
                categoryService.getItemCategories(
                        token,
                        page,size, name, code,year,currentYearBudget,productCount,
                        warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/sub-categories")
    public ResponseEntity<Object> getSubItemCategories(
            @AuthenticationPrincipal Jwt token,
                     @RequestParam("page") Optional<Integer> page,
                     @RequestParam("size") Optional<Integer> size,
                     @RequestParam("name")  Optional<String> name,
                     @RequestParam("code") Optional<String> code,
                     @RequestParam("year") Optional<Integer> year,
                     @RequestParam("currentYearBudget") Optional<BigDecimal> currentYearBudget,
                     @RequestParam("productCount") Optional<Long> productCount,
                     @RequestParam("categoryId") Optional<Long> categoryId,
                     @RequestParam("warehouseId") Optional<Long> warehouseId,
                     @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId

    ){
        return new ResponseEntity<>(
                categoryService.getItemCategories(token,page,size, name, code,year,currentYearBudget,productCount,categoryId,
                        warehouseId,warehouseStoreId),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/main-categories")
    public ResponseEntity<Object> getMainCategoryListForInventoryControl(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getCategoriesForInventoryControl(token,warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/main-categories")
    public ResponseEntity<Object> getMainCategoryList(
                                             @RequestParam("warehouseId") Optional<Long> warehouseId,
                                             @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId,
                                             @RequestParam("name")  Optional<String> name,
                                             @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getCategories(warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/main-categories/pages")
    public ResponseEntity<Object> getMainCategoryPage(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(
                categoryService.getCategories(token, warehouseId,warehouseStoreId,name,code,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getCategoryList(@RequestParam("storeId") Optional<Long> storeId,
                                            @RequestParam("categoryId")  Optional<Long> categoryId,
                                             @RequestParam("name")  Optional<String> name,
                                                @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategories(storeId,categoryId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllCategoryList(@RequestParam("categoryId")  Optional<Long> categoryId,
                                             @RequestParam("name")  Optional<String> name,
                                             @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategoriesAll(categoryId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/list")
    public ResponseEntity<Object> getSubCategoryListForInventoryControl(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getSubCategoriesForInventoryControl(
                        token,
                        categoryId,warehouseId,storeId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/pages")
    public ResponseEntity<Object> getSubCategoriesForInventoryControl(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryService.getSubCategoriesForInventoryControl(
                        token,
                        categoryId,warehouseId,storeId, name,code,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/list/pending")
    public ResponseEntity<Object> getPendingSubCategoryListForInventoryControl(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code){
        return new ResponseEntity<>(
                categoryService.getPendingSubCategoriesForInventoryControl(
                        token,
                        categoryId,warehouseId,storeId, name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/pending")
    public ResponseEntity<Object> getPendingSubCategoryPendingForInventoryControl(
            @AuthenticationPrincipal Jwt token,
            @RequestParam("categoryId")  Optional<Long> categoryId,
            @RequestParam("warehouseId")  Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId")  Optional<Long> storeId,
            @RequestParam("name")  Optional<String> name,
            @RequestParam("code") Optional<String> code,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
            ){
        return new ResponseEntity<>(
                categoryService.getPendingSubCategoriesForInventoryControl(
                        token,
                        categoryId,warehouseId,storeId, name,code,page,size),
                HttpStatus.OK
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemCategory(
        // @ApiParam(value = "Category Id",example = "1", required = true) 
        @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getItemCategoryDetail(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/pending/{id}")
    public ResponseEntity<Object> getPendingItemCategory(
            // @ApiParam(value = "Category Id",example = "1", required = true)
            @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getPendingItemCategory(id),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/{id}")
    public ResponseEntity<Object> getItemCategoryForInventoryControl(
        // @ApiParam(value = "Category Id",example = "1", required = true) 
        @PathVariable("id") Long id){
        return new ResponseEntity<>(
                categoryService.getItemCategoryForInventoryControl(id),
                HttpStatus.OK
        );
    }


    @PostMapping("/bulk-create")
    public ResponseEntity<Void> createCategories(
            @AuthenticationPrincipal Jwt token,
            @RequestBody BulkCategoryRequestDto categoryRequestDto){
        categoryService.addCategories(token,categoryRequestDto.getCategories());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<Void> createItemCategory(
        @AuthenticationPrincipal Jwt loggedInUser,
        @RequestBody @Valid CategoryRequestDto categoryRequestDto){
        categoryService.addCategory(loggedInUser, categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItemCategory(
        @PathVariable("id") Long id,
        @RequestBody CategoryRequestDto categoryRequestDto){
        categoryService.updateCategory(id,categoryRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemCategory(
            @PathVariable("id") Long id,
            @RequestParam("warehouseId") Long warehouseId,
            @RequestParam("warehouseStoreId") Long warehouseStoreId
    ){
        categoryService.deleteCategory(id,warehouseId,warehouseStoreId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<Void> activeItemCategory(
        @PathVariable("id") Long id,
        @RequestParam("warehouseId") Long warehouseId,
        @RequestParam("storeId") Long warehouseStoreId
    ){
        categoryService.activeCategory(id, warehouseId, warehouseStoreId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/inventory-control/main-categories/pending")
    public ResponseEntity<Object> getPendingCategories(
            @AuthenticationPrincipal Jwt token,
            Optional<Long> warehouseId,
            Optional<Long> warehouseStoreId,
            Optional<String> name,
            Optional<String> code
    ){
        return new ResponseEntity<>(
                categoryService.getPendingCategories(token,warehouseId,warehouseStoreId,name,code),
                HttpStatus.OK
        );
    }

    @GetMapping("/inventory-control/main-categories/pending/pages")
    public ResponseEntity<Object> getPendingCategories(
            @AuthenticationPrincipal Jwt token,
            Optional<Long> warehouseId,
            Optional<Long> warehouseStoreId,
            Optional<String> name,
            Optional<String> code,
            Optional<Integer> page,
            Optional<Integer> size
    ){
        return new ResponseEntity<>(
                categoryService.getPendingCategories(token,warehouseId,warehouseStoreId,name,code,page,size),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{categoryId}/{attributeId}")
    public ResponseEntity<Void> deleteCategoryAttribute(
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("attributeId") Long attributeId

    ){
        categoryService.deleteAttribute(categoryId,attributeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @GetMapping("/next-id")
    public ResponseEntity<Object> getNextId(){
        Map<String,Object> response = new HashMap<>();
        response.put("code",categoryService.getNewCategoryCode());
        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }

    @PutMapping("/approve/category/{id}")
    public ResponseEntity<Void> approveCategory(
        @AuthenticationPrincipal Jwt token,
        @PathVariable("id") Long id,
        @RequestBody CategoryApproveRequestDto categoryApproveRequestDto){
        categoryService.approveItemCategory(token, id, categoryApproveRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/approve/subcategory/{id}")
    public ResponseEntity<Void> approveSubCategory(
        @AuthenticationPrincipal Jwt token,
        @PathVariable("id") Long id,
        @RequestBody CategoryApproveRequestDto categoryApproveRequestDto){
        categoryService.approveItemCategory(token,  id, categoryApproveRequestDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importItems(
            @RequestPart("file") Optional<MultipartFile> file
    ){
        categoryService.importCategories(file);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/download-template")
    public ResponseEntity<Object> downloadTemplate(
            @RequestParam("categoryId") Optional<Long> categoryId,
            @RequestParam("warehouseId") Optional<Long> warehouseId,
            @RequestParam("warehouseStoreId") Optional<Long> warehouseStoreId
    ){

        return new ResponseEntity<>(categoryService.getTemplateData(
                    categoryId.orElse(null),
                    warehouseId.orElse(null),
                    warehouseStoreId.orElse(null)
            ),
            HttpStatus.OK
        );
    }
}
