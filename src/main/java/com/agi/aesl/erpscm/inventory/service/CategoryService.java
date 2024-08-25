package com.agi.aesl.erpscm.inventory.service;

// import com.agi.aesl.erpscm.authentication.dto.ClaimResponseDto;

import com.agi.aesl.erpscm.inventory.dto.request.CategoryApproveRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDtoCustom;
import com.agi.aesl.erpscm.inventory.entity.CategoryWarehouseStore;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Optional<ItemCategory> addCategory(Jwt loggedInUser, CategoryRequestDto categoryRequestDto);

    void addCategories(Jwt token,List<CategoryRequestDtoCustom> categoryRequestDtos);

    void updateCategory(Long id, CategoryRequestDto categoryRequestDto);

    Optional<ItemCategory> existByCode(String Code);

    Optional<ItemCategory> getItemCategory(Long id);

    Optional<ItemCategory> getAnyItemCategory(Long id);
    Optional<ItemCategory> getPendingItemCategory(Long id);


    Page<?> getItemCategories(Jwt token,
                              Optional<Integer> page, Optional<Integer> size,
                              Optional<String> name, Optional<String> code,
                              Optional<BigDecimal> currentYearBudget,
                              Optional<Long> productCount,
                              Optional<Long> warehouseId,
                              Optional<Long> warehouseStoreId
    );

    Page<?> getItemCategories(Jwt token, Optional<Integer> page, Optional<Integer> size,
                              Optional<String> name, Optional<String> code,
                              Optional<BigDecimal> currentYearBudget, Optional<Long> productCount,
                              Optional<Long> categoryId,
                              Optional<Long> warehouseId,
                              Optional<Long> warehouseStoreId
    );

    void activeCategory(Long id, Long warehouseId, Long storeId);

    void deleteCategory(Long id, Long warehouseId, Long storeId);

    List<?> getCategories(Optional<Long> warehouseId, Optional<Long> warehouseStoreId, Optional<String> name, Optional<String> code);

    List<?> getCategoriesForInventoryControl(Jwt token,Optional<Long> warehouse, Optional<Long> warehouseStore, Optional<String> name, Optional<String> code);


    List<?> getSubCategories(Optional<Long> storeId, Optional<Long> categoryId, Optional<String> name, Optional<String> code);

    List<?> getSubCategoriesAll(Optional<Long> categoryId, Optional<String> name, Optional<String> code);

    List<?> getSubCategoriesForInventoryControl(
            Jwt token,
            Optional<Long> categoryId,
            Optional<Long> warehouseId,
            Optional<Long> storeId,
            Optional<String> name,
            Optional<String> code);

    String getNewCategoryCode();

    void deleteAttribute(Long categoryId, Long attributeId);

    Optional<ItemCategory> getItemCategoryForInventoryControl(Long id);

//    Optional<CategoryWarehouseStore> getCategoryByCodeAndStore(Long warehouseId, CopyToStoreDto copyToStoreDto, CategoryRequestDto categoryRequestDto, Long storeId);

//    void validateCategorySubCategoryRelation(ItemCategory category, ItemCategory subCategory);

    Optional<ItemCategory> getItemCategoryByName(String catName);

    Optional<ItemCategory> getCategoryByCode(String subCategoryCode);

    List<?> getPendingCategories(Jwt token, Optional<Long> warehouseId,
                                 Optional<Long> warehouseStoreId,
                                 Optional<String> name,
                                 Optional<String> code);

    List<?> getPendingSubCategoriesForInventoryControl(
            Jwt token,
            Optional<Long> categoryId, Optional<Long> warehouseId,
                                                       Optional<Long> storeId, Optional<String> name,
                                                       Optional<String> code);

    void approveItemCategory(Jwt token, Long id, CategoryApproveRequestDto categoryApproveRequestDto);

    void validateCategorySubCategoryRelation(ItemCategory category, ItemCategory subCategory);
}
