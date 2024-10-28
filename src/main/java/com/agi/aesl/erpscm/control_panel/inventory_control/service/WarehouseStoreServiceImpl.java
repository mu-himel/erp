package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
// import com.agi.aesl.erpscm.control_panel.inventory_control.dto.CopyToStoreDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.dto.StoreDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.entity.CategoryWarehouseStore;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemStockRepository;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WarehouseStoreServiceImpl implements WarehouseStoreService{

    public static final int SIZE = 10;
    @Autowired
    private WarehouseStoreRepository warehouseStoreRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private CategoryWarehouseStoreRepository cwsRepository;

    @Autowired
    private ItemStockRepository itemStockRepository;

    @Override
    @Transactional
    public void deleteWarehouseStore(Long id) {
        Optional<WarehouseStore> warehouseStoreOptional = warehouseStoreRepository.findById(id);
        List<CategoryWarehouseStore> warehouseStoreOp = cwsRepository.findByWarehouseStoreId(id);
        if(!warehouseStoreOp.isEmpty()){
            throw new RuntimeException("Sorry! Store has some category or sub category ");
        }
        List<ItemStock> itemStockOp = itemStockRepository.findByWarehouseStoreId(id);
        if(!itemStockOp.isEmpty()){
            throw new RuntimeException("Sorry! Store has some items");
        }
        warehouseStoreOptional.ifPresent(warehouseStore -> warehouseStore.setActive(false));
    }

    @Override
    public Page<?> getStores(Long warehouseId, Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(SIZE));
        return warehouseStoreRepository.findAllByActiveAndWarehouseId(true,warehouseId,pageable);
    }

    @Override
    public List<?> getStoresByWarehouse(Jwt token, Optional<Long> warehouseId) {
        claimResolver.setToken(token);
        String uri = "inventory-control/store";
        List<Long> ids = new ArrayList<>();
        if(warehouseId.isPresent()) {
            ids.add(warehouseId.get());
        }else {
            DataFilter dataFilter = new DataFilter(uri, claimResolver);
            dataFilter.setReaderService(integrationReaderService);
            ids = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }
        return warehouseStoreRepository.findAllByWarehouseId(ids);
    }

    @Override
    public List<WarehouseStore> getStoresByWarehouseId(Long warehouseId) {
        return warehouseStoreRepository.findAllByWarehouseId(warehouseId);
    }

    @Override
    public Optional<?> getStore(Long warehouseStoreId) {
        return warehouseStoreRepository.findStoreById(warehouseStoreId);
    }

    @Override
    public Optional<WarehouseStore> getStoreById(Long warehouseStoreId) {
        return warehouseStoreRepository.findById(warehouseStoreId);
    }

    // @Override
    // Deprecated
    // public void copyToStore(Long wId, CopyToStoreDto copyToStoreDto) {

    //     Optional<WarehouseStoreRepository.WarehouseStoreInfoSingle> warehouseStoreOptional = warehouseStoreRepository
    //                                         .findByIdAndWarehouseId(copyToStoreDto.getStore().getId(), wId);

    //     if(warehouseStoreOptional.isEmpty()){
    //         throw new AesException("Store Not Found");
    //     }
    //     WarehouseStoreRepository.WarehouseStoreInfoSingle warehouseStoreInfoSingle = warehouseStoreOptional.get();

    //     copyToStoreDto.getSubCategories().stream().forEach(categoryRequestDto -> {
    //         Optional<CategoryWarehouseStore> itemCategoryOptional = categoryService
    //                 .getCategoryByCodeAndStore(wId,copyToStoreDto,categoryRequestDto, warehouseStoreInfoSingle.getId());

    //         if(itemCategoryOptional.isEmpty()){
    //             categoryRequestDto.setWarehouse(new ReferenceObjectDto(wId));
    //             categoryRequestDto.setWarehouseStore(new ReferenceObjectDto(copyToStoreDto.getStore().getId()));
    //             categoryService.addCategory(null,categoryRequestDto);
    //         }
    //     });

    // }

    @Override
    public List<?> getStoreSubCategories(Long warehouseId ,Long storeId) {
        Optional<WarehouseStoreRepository.WarehouseStoreInfoSingle> warehouseStoreOptional = warehouseStoreRepository
                .findByIdAndWarehouseId(storeId,warehouseId);
        if(warehouseStoreOptional.isEmpty()){
            throw new AesException("Store not found");
        }

        WarehouseStoreRepository.WarehouseStoreInfoSingle warehouseStoreInfoSingle = warehouseStoreOptional.get();
        return categoryService.getSubCategories(warehouseStoreInfoSingle.getId().describeConstable(),
                Optional.ofNullable(null),
                Optional.ofNullable(null),Optional.ofNullable(null));
    }

    @Override
    @Transactional
    public void createStore(StoreDto storeDto) {
        WarehouseStore warehouseStore = new WarehouseStore();
        Optional<Warehouse> warehouseOp = warehouseRepository.findById(storeDto.getWarehouseId());
        if(warehouseOp.isEmpty()){
            throw new AesException("Sorry! Warehouse not found");
        }
        Warehouse warehouse = warehouseOp.get();
        Optional<WarehouseStore> storeOp = warehouseStoreRepository.findByStoreNameAndWarehouseIdAndActive(storeDto.getName(),
                warehouse.getId(),true);
        if(storeOp.isPresent()){
            throw new RuntimeException("Sorry! Store already exist with this name in this warehouse");
        }
        warehouseStore.setStoreName(storeDto.getName());
        warehouseStore.setWarehouse(warehouse);
        warehouseStore.setActive(true);
        warehouseStore.setAlias(warehouse.getName().toLowerCase()+" - "+storeDto.getName().toLowerCase());
        warehouseStoreRepository.save(warehouseStore);
    }

    @Override
    @Transactional
    public void updateStore(Long id, StoreDto storeDto) {
        Optional<WarehouseStore> warehouseStoreOp = warehouseStoreRepository.findById(id);
        if(warehouseStoreOp.isEmpty()){
            throw new AesException("Sorry! Warehouse Store not found");
        }
        Optional<Warehouse> warehouseOp = warehouseRepository.findById(storeDto.getWarehouseId());
        if(warehouseOp.isEmpty()){
            throw new AesException("Sorry! Warehouse not found");
        }
        Warehouse warehouse = warehouseOp.get();
        Optional<WarehouseStore> storeOp = warehouseStoreRepository.findByStoreNameAndWarehouseIdAndActive(storeDto.getName(),
                warehouse.getId(),true);
        if(storeOp.isPresent()){
            if(!storeOp.get().getId().equals(id)){
                throw new RuntimeException("Sorry! This store name already exist with different #ID["+storeOp.get().getId()+"]");
            }
        }

        WarehouseStore warehouseStore = warehouseStoreOp.get();

        warehouseStore.setAlias(storeDto.getName().toLowerCase()+" - "+warehouse.getName().toLowerCase());
        warehouseStore.setStoreName(storeDto.getName());
        warehouseStore.setWarehouse(warehouse);
        
    }

        

    
}
