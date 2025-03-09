package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<?> getWarehouses(Optional<String> name, Optional<Integer>page,
                                                                Optional<Integer>size){

         Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(Integer.MAX_VALUE));
        return warehouseStoreService.getFinishedGoodsStoreWarehouses(name,pageable);
    }

    @Override
    public Page<?> getCategories(Optional<Long> warehouseId, Optional<String> name, Optional<String> code,
                                 Optional<Integer>page,Optional<Integer>size) {
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()) {
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = warehouseStoreService.getFinishedGoodsStoreWarehousesIs();
        }

        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseIdIn(warehouseIds);
        List<Long> finisGoodStoreIds = new ArrayList<>();
        stores.stream().forEach(s -> {
          if( s.getStoreName().toLowerCase().contains("finish")){
              finisGoodStoreIds.add(s.getId());
          }
        });
        if(!finisGoodStoreIds.isEmpty()){
            Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(Integer.MAX_VALUE));
//            List<CategoryInfo> mainCategories = new ArrayList<>();
            Page<CategoryRepository.SalesCategoryInfo> allMainCategories = categoryRepository
                        .findAllMainCategoriesForSales(warehouseIds, finisGoodStoreIds, name.orElse(null),
                                code.orElse(null),pageable);
//                allMainCategories.stream().forEach(mc->{
//                    mainCategories.add(new CategoryInfo(mc.getId(),mc.getName(),mc.getCode(),mc.getWarehouseName(),0L));
//                });
            return allMainCategories;
        }

        return Page.empty();
    }

    @Override
    public Page<?> getSubCategories(Optional<Long> warehouseId, Optional<Long> categoryId,
                                    Optional<String> name, Optional<String> code,
                                    Optional<Integer> page, Optional<Integer> size) {
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()) {
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = warehouseStoreService.getFinishedGoodsStoreWarehousesIs();
        }
        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseIdIn(warehouseIds);
        List<Long> finisGoodStoreIds = new ArrayList<>();
         stores.stream().forEach(s -> {
            if(s.getStoreName().toLowerCase().contains("finish")){
                finisGoodStoreIds.add(s.getId());
            }
        });
        if(!finisGoodStoreIds.isEmpty()){
            Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(Integer.MAX_VALUE));
            List<SubCategoryInfo> subCategories = new ArrayList<>();
            Page<CategoryRepository.SalesSubCategoryInfo> allSubCategories = categoryRepository
                    .findAllSubCategories(finisGoodStoreIds, categoryId,name.orElse(null),
                            code.orElse(null), pageable);
            return allSubCategories;
        }
        return Page.empty();
    }

    @Override
    public List<SubCategoryInfo> getSubCategories(Long warehouseId, String categoryCode) {
        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseId(warehouseId);
        Optional<WarehouseStore> finishGood = stores.stream().filter(s -> {
            return  s.getStoreName().toLowerCase().contains("finish");
        }).findFirst();
        if(finishGood.isPresent()){
            List<SubCategoryInfo> subCategories = new ArrayList<>();
            List<CategoryRepository.ItemCategoryInfo> allSubCategories = categoryRepository
                    .findAllSubCategories(finishGood.get().getId(), categoryCode, null, null);

            allSubCategories.stream().forEach(sc->{
                subCategories.add(new SubCategoryInfo(sc.getId(),sc.getName(),sc.getCode(),sc.getParentCategoryCode()));
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
            return  s.getStoreName().toLowerCase().contains("finish");
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

    @Override
    public Page<?> getProducts(Optional<Long> warehouseId, Optional<Long> categoryId,
                               Optional<Long> subCategoryId,
                               Optional<Integer> page, Optional<Integer> size) {
        List<Long> categoryIds = new ArrayList<>();
        List<Long> subCategoryIds = new ArrayList<>();
        List<Long> warehouseIds = new ArrayList<>();
        List<ProductInfo> products = new ArrayList<>();
        if(categoryId.isPresent()){
            Optional<ItemCategory> catOp = categoryRepository.findById(categoryId.get());
            if(catOp.isEmpty()){
                throw new RuntimeException("Sorry! category not found");
            }
            ItemCategory category = catOp.get();
            categoryIds.add(category.getId());
        }
        if(subCategoryId.isPresent()) {
            Optional<ItemCategory> subCatOp = categoryRepository.findById(subCategoryId.get());
            if(subCatOp.isEmpty()){
                throw new RuntimeException("Sorry! sub category not found");
            }
            ItemCategory subCategory = subCatOp.get();
            subCategoryIds.add(subCategory.getId());
        }


        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else{
            warehouseIds = warehouseStoreService.getFinishedGoodsStoreWarehousesIs();
        }


        List<WarehouseStore> stores = warehouseStoreService.getStoresByWarehouseIdIn(warehouseIds);
        List<Long> finisGoodStoreIds = new ArrayList<>();
        stores.stream().forEach(s -> {
            if(s.getStoreName().toLowerCase().contains("finish")){
                finisGoodStoreIds.add(s.getId());
            }
        });
        if(!finisGoodStoreIds.isEmpty()){
            Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(Integer.MAX_VALUE));
            Page<ItemRepository.SalesItems> allItemList = itemRepository.findAllItemListForSales(null, null, null, null,
                    categoryIds, subCategoryIds, warehouseIds, finisGoodStoreIds,pageable);
//            allItemList.stream().forEach(i->{
//                String itemName = i.getSubCategoryName()+" - "+i.getSubCategoryCode()+" - "+i.getItemAttributeName();
//                products.add(new ProductInfo(i.getId(),i.getName(),i.getCategoryCode(),i.getCategoryName(),
//                        i.getSubCategoryCode(),i.getSubCategoryName(),i.getItemUnit(),i.getCode(),itemName,i.getQty().toBigInteger()));
//            });
            return allItemList;
        }

        return Page.empty();
    }
}
