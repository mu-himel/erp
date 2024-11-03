package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SalesInventoryServiceImpl implements SalesInventoryService{

    @Autowired
    private WarehouseStoreService warehouseStoreService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public List<CategoryInfo> getCategories(Long warehouseId) {
        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseId(warehouseId);
        Optional<WarehouseStore> finish_good = stores.stream().filter(s -> {
          return  s.getStoreName().toLowerCase().contains("finish good");
        }).findFirst();
        if(finish_good.isPresent()){
            List<CategoryInfo> mainCategories = new ArrayList<>();
            List<CategoryRepository.MainCategoriesInfo> allMainCategories = categoryRepository
                        .findAllMainCategories(warehouseId, finish_good.get().getId(), null, null);
            allMainCategories.stream().forEach(mc->{
                mainCategories.add(new CategoryInfo(mc.getId(),mc.getName(),mc.getCode()));
            });
            return mainCategories;
        }

        return new ArrayList<>();
    }

    @Override
    public List<CategoryInfo> getSubCategories(Long warehouseId, Long categoryId) {
        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseId(warehouseId);
        Optional<WarehouseStore> finish_good = stores.stream().filter(s -> {
            return  s.getStoreName().toLowerCase().contains("finish good");
        }).findFirst();
        if(finish_good.isPresent()){
            List<CategoryInfo> subCategories = new ArrayList<>();
            List<CategoryRepository.ItemCategoryInfo> allSubCategories = categoryRepository
                    .findAllSubCategories(finish_good.get().getId(), categoryId, null, null);

            allSubCategories.stream().forEach(sc->{
                subCategories.add(new CategoryInfo(sc.getId(),sc.getName(),sc.getCode()));
            });
            return subCategories;
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProductInfo> getProducts(Long warehouseId, Long categoryId, Long subCategoryId) {
        List<Long> categoryIds = new ArrayList<>();
        List<Long> warehouseIds = new ArrayList<>();
        List<ProductInfo> products = new ArrayList<>();
        Optional<ItemCategory> catOp = categoryRepository.findById(categoryId);
        Optional<ItemCategory> subCatOp = categoryRepository.findById(subCategoryId);
        if(catOp.isEmpty()){
            throw new RuntimeException("Sorry! category not found");
        }
        if(subCatOp.isEmpty()){
            throw new RuntimeException("Sorry! sub category not found");
        }
        ItemCategory category = catOp.get();
        ItemCategory subCategory = subCatOp.get();
        categoryIds.add(category.getId());
        warehouseIds.add(warehouseId);
        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseId(warehouseId);
        Optional<WarehouseStore> finish_good = stores.stream().filter(s -> {
            return  s.getStoreName().toLowerCase().contains("finish good");
        }).findFirst();
        if(finish_good.isPresent()){
            List<ItemRepository.PageItemList> allItemList = itemRepository.findAllItemList(null, null, null, null,
                    categoryIds, subCategory.getId(), warehouseIds, finish_good.get().getId());
            allItemList.stream().forEach(i->{
                String itemName = i.getSubCategoryName()+" - "+i.getSubCategoryCode()+" - "+i.getItemAttributeName();
                products.add(new ProductInfo(i.getId(),i.getName(),i.getCategoryCode(),i.getCategoryName(),
                        i.getSubCategoryCode(),i.getSubCategoryName(),i.getItemUnit(),i.getCode(),itemName,i.getQty().toBigInteger()));
            });
            return products;
        }

        return new ArrayList<>();
    }
}
