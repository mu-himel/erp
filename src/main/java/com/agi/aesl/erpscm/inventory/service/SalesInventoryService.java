package com.agi.aesl.erpscm.inventory.service;


import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import org.springframework.data.domain.Page;

public interface SalesInventoryService {




    record CategoryInfo(Long id, String name,String code, String warehouseName,Long subcategoriesCount){}
    record SubCategoryInfo(Long id, String name,String code, String categoryCode){}
    record ProductInfo(Long id, String brand,
                       String categoryCode,
                       String categoryName,
                       String subCategoryCode,
                       String subcategoryName,
                       String itemUnit,
                       String itemCode,String itemName,
                       BigInteger stockQty){}

    Page<WarehouseRepository.WarehouseInfoExt> getWarehouses(Optional<String>name, Optional<Integer> page, Optional<Integer> size);
    Page<CategoryRepository.SalesCategoryInfo> getCategories(Optional<Long> warehouseId, Optional<String> name, Optional<String> code,
                                                             Optional<Integer> page, Optional<Integer> size);
    Page<CategoryRepository.SalesSubCategoryInfo> getSubCategories(Optional<Long> warehouseId, Optional<Long> categoryId,
                                                                   Optional<String> name, Optional<String> code,
                                                                   Optional<Integer> page, Optional<Integer> size);

    List<SubCategoryInfo> getSubCategories(Long warehouseId, String categoryCode);

    List<ProductInfo> getProducts(Long warehouseId, Long categoryId, Long subCategoryId);
    Page<ItemRepository.SalesItems> getProducts(Optional<Long> warehouseId, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                Optional<Integer> page, Optional<Integer> size);
}
