package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.account_finance.service.AccountService;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseStoreService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDetail;
import com.agi.aesl.erpscm.inventory.dto.response.SyncItemDto;
import com.agi.aesl.erpscm.inventory.entity.*;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemImportLogRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Service
public class ItemSyncService {

    private ClaimResolver claimResolver;
    private OrgService orgService;
    private NetworkService networkService;
    private CpsServerConfig cpsConfig;
    private AccountService accountService;
    private ItemService itemService;
    private CategoryService categoryService;
    private WarehouseService warehouseService;
    private WarehouseStoreService warehouseStoreService;
    private ItemRepository itemRepository;
    private ItemImportLogRepository itemImportLogRepository;
    private CategoryBrandRepository categoryBrandRepository;

    private static final String ERR_WAREHOUSE_STORE_NOT_FOUND="Sorry! Warehouse Store not found";
    private static final String ERR_WAREHOUSE_NOT_FOUND="Sorry! Warehouse not found";

    @Transactional
    public List<SyncItemDetail> fetchItemsBySubCat(Jwt token, String subCatCode,String orgIdKey){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        if(orgOp.isPresent()){
            headers.setBearerAuth(token.getTokenValue());
            headers.set(orgIdKey,orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<?> payload = new HttpEntity<>(headers);
        String url = cpsConfig.getItemFetchEndpoint(subCatCode);
        ResponseEntity<?> response = networkService.get(url, payload, SyncItemDto.class);
        if(response.getStatusCode()!= HttpStatus.OK){
            throw new AesException("Unable to fetch Items from CPS");
        }
        var responseBody = response.getBody();

        SyncItemDto syncItemDto = (SyncItemDto)responseBody;

        return (syncItemDto!=null)?syncItemDto.getItems() : new ArrayList<>();
    }

    private Warehouse validWarehouse(Long warehouseId){
        Optional<Warehouse> warehouseOp = warehouseService.getWarehouse(warehouseId);
        if(warehouseOp.isEmpty()){
            throw new AesException(ERR_WAREHOUSE_NOT_FOUND);
        }
        return warehouseOp.get();
    }

    private WarehouseStore validWarehouseStore(Long warehouseStoreId){
        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreService.getStoreById(warehouseStoreId);
        if(warehouseStoreOp.isEmpty()){
            throw new AesException(ERR_WAREHOUSE_STORE_NOT_FOUND);
        }
        return warehouseStoreOp.get();
    }

    private ItemCategory validSubCategory(String subCategoryCode){
        Optional<ItemCategory> subCatOp = categoryService.getCategoryByCode(subCategoryCode);
        if(subCatOp.isEmpty()){
            throw new AesException("Sorry! Sub Category not found");
        }
        return subCatOp.get();
    }

    private CategoryBrand validCategoryBrand(SyncItemDetail syncItemDetail, ItemCategory subCat){
        CategoryBrand catBrand = null;
        if(syncItemDetail.getBrand()!=null) {
            Optional<CategoryBrand> catBrandOp = categoryBrandRepository.findByCategoryIdAndName(subCat.getId(), syncItemDetail.getBrand().name());
            if (catBrandOp.isEmpty()) {
                catBrand = new CategoryBrand();
                catBrand.setCategory(subCat);
                catBrand.setName(syncItemDetail.getBrand().name());
                categoryBrandRepository.save(catBrand);
            } else {
                catBrand = catBrandOp.get();
            }
        }else{
            throw new AesException("Brand Information Required during Sync");
        }
        return catBrand;
    }

    private void setItemStatusBasedOnImportLog(List<Item> items, Long warehouseId){
        for(Item i : items){
            Optional<ItemImportLog> itemImportExistOp = itemImportLogRepository.findByItemIdAndWarehouseId(i.getId(), warehouseId);
            if(itemImportExistOp.isPresent()){
                ItemImportLog iil = itemImportExistOp.get();

                i.setActive(iil.getItemInactiveStatus().equals(ItemInactiveStatus.APPROVED));

            }else{
                i.setActive(false);
            }
        }
    }


    private void updateItemStock(List<ItemStock> stocks, Item item, Warehouse warehouse, WarehouseStore warehouseStore){
        if(stocks.isEmpty()) {
            stocks.add(new ItemStock(
                    new BigDecimal(0L),
                    item,
                    StockType.STOCK_IN,
                    warehouse,
                    warehouseStore
            ));
            item.setStocks(stocks);
        }else{
            boolean warehouseExist=false;
            for(ItemStock s : stocks){
                if(s.getWarehouse().getId().equals(warehouse.getId())){
                    warehouseExist=true;
                }
                Optional<ItemImportLog> importLogExist = itemImportLogRepository.findByItemIdAndWarehouseId(
                        item.getId(),s.getWarehouse().getId()
                );
                if(importLogExist.isPresent()) {
                    ItemImportLog itemImportLog = importLogExist.get();
                    ItemImportLog iil = new ItemImportLog();
                    iil.setItem(itemImportLog.getItem());
                    iil.setWarehouse(warehouse);
                    iil.setItemInactiveStatus(itemImportLog.getItemInactiveStatus());
                    itemImportLogRepository.save(iil);
                }
            }
            if(Boolean.FALSE.equals(warehouseExist)){
                stocks.add(new ItemStock(
                        new BigDecimal(0L),
                        item,
                        StockType.STOCK_IN,
                        warehouse,
                        warehouseStore
                ));
                item.setStocks(stocks);
            }
        }
    }

    private void setAttributes(SyncItemDetail syncItemDetail, Item item){
        if(syncItemDetail.getAttributes()!=null && !syncItemDetail.getAttributes().isEmpty()) {

            item.setAttributes(syncItemDetail.getAttributes().stream().map(itemAttribute -> {

                itemAttribute.setId(null);
                itemAttribute.setItem(item);
                return itemAttribute;
            }).toList());
        }
    }

    /**
     * @Description import from cps
     * @param warehouseId
     * @param warehouseStoreId
     * @param syncItemDetail
     * @return
     */
    @Transactional
    public Item createItem(Jwt token, Long warehouseId, Long warehouseStoreId, SyncItemDetail syncItemDetail){
        claimResolver.setToken(token);
        Item item = syncItemDetail.getEntity();
        String itemAttributeName = itemService.generateItemAttributeName(syncItemDetail.getAttributes());



        Warehouse warehouse = validWarehouse(warehouseId);
        WarehouseStore warehouseStore = validWarehouseStore(warehouseStoreId);

        item.setCode(warehouseStore.getStoreName().substring(0,1).toUpperCase()+"-"+item.getCode());

        String subCategoryCode = warehouseStore.getStoreName().substring(0,1).toUpperCase()+"-"+syncItemDetail.getItemCategory().code();
        // Get Subcategory By Code
        ItemCategory subCat = validSubCategory(subCategoryCode);

        // Get Category Brand
        CategoryBrand catBrand = validCategoryBrand(syncItemDetail,subCat);



        item.setItemCategory(subCat);
        item.setItemParentCategory(subCat.getParentCategory());
        item.setBrand(catBrand);
        item.setCpsItemId(syncItemDetail.getId());

        List<?> itemExistByAttr = itemService.getByAttributes(catBrand.getId(),itemAttributeName,subCat.getId(),warehouseId);
        if(!itemExistByAttr.isEmpty()){
            List<Item> items = itemRepository.findByBrandIdAndItemCategoryIdAndItemAttributeName(catBrand.getId(), subCat.getId(), itemAttributeName);
            setItemStatusBasedOnImportLog(items,warehouseId);
        } else {

            Optional<Item> itemExistByCode = itemRepository.findByCode(item.getCode());
            if(itemExistByCode.isPresent()){
                item = itemExistByCode.get();
                List<ItemStock> stocks = item.getStocks();
                updateItemStock(stocks,item,warehouse,warehouseStore);


            }else{
                item.setItemUnit(syncItemDetail.getItemUnit());
                item.setManufacturer(syncItemDetail.getManufacturer());
                item.setName(syncItemDetail.getName());
                item.setItemAttributeName(itemAttributeName);
                item.setActive(false);
                item.setStocks(List.of(new ItemStock(
                        new BigDecimal(0L),
                        item,
                        StockType.STOCK_IN,
                        warehouse,
                        warehouseStore
                )));

                setAttributes(syncItemDetail,item);

                if(syncItemDetail.getFunctionalUnits()!=null && !syncItemDetail.getFunctionalUnits().isEmpty()) {
                    Item finalItem = item;
                    item.setItemFunctionalUnits(syncItemDetail.getFunctionalUnits().stream().map(itemFunctionalUnit -> {
                        itemFunctionalUnit.setId(null);
                        itemFunctionalUnit.setItem(finalItem);
                        return itemFunctionalUnit;
                    }).toList());
                }

                itemRepository.save(item);

                Optional<ItemImportLog> importLogExist = itemImportLogRepository.findByItemIdAndWarehouseId(item.getId(),warehouseId);
                if(importLogExist.isEmpty()) {
                    ItemImportLog iil = new ItemImportLog();
                    iil.setItem(item);
                    iil.setWarehouse(warehouse);
                    iil.setItemInactiveStatus(ItemInactiveStatus.PENDING_VERIFICATION);
                    // added for inactive account service
                    item.setActive(false);
                    itemImportLogRepository.save(iil);
                }
                accountService.setItemService(itemService);
                accountService.createItemLedger(claimResolver,item,warehouse,warehouseStore);
            }




        }
        return item;
    }
}
