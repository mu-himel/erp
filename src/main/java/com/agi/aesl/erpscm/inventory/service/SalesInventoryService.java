package com.agi.aesl.erpscm.inventory.service;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface SalesInventoryService {




    record CategoryInfo(Long id, String name,String code){}
    record SubCategoryInfo(Long id, String name,String code, String categoryCode){}
    record ProductInfo(Long id, String brand,
                       String categoryCode,
                       String categoryName,
                       String subCategoryCode,
                       String subcategoryName,
                       String itemUnit,
                       String itemCode,String itemName,
                       BigInteger stockQty){}
    List<CategoryInfo> getCategories(Long warehouseId);
    List<SubCategoryInfo> getSubCategories(Long warehouseId,Long categoryId);

    List<SubCategoryInfo> getSubCategories(Long warehouseId, String categoryCode);

    List<ProductInfo> getProducts(Long warehouseId, Long categoryId, Long subCategoryId);
}
