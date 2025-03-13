package com.agi.aesl.erpscm.control_panel.inventory_control.service;


import com.agi.aesl.erpscm.control_panel.inventory_control.dto.StoreDto;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface WarehouseStoreService {

    void deleteWarehouseStore(Long id);

    Page<WarehouseStoreRepository.WarehouseStoreInfoV2> getStores(Long warehouseId, Optional<Integer> page, Optional<Integer> size);

    List<WarehouseStoreRepository.WarehouseStoreInfo> getStoresByWarehouse(Jwt token, Optional<String> name, Optional<Long> warehouseId);
    List<WarehouseStore> getStoresByWarehouseId( Long warehouseId);
    List<WarehouseStore> getStoresByWarehouseIdIn( List<Long> warehouseId);

    Optional<WarehouseStoreRepository.WarehouseStoreInfoSingle> getStore(Long warehouseStoreId);
    Optional<WarehouseStore> getStoreById(Long warehouseStoreId);

    Page<WarehouseRepository.WarehouseInfoExt> getFinishedGoodsStoreWarehouses(Optional<String> name, Pageable pageable);

    List<CategoryRepository.ItemCategoryInfo> getStoreSubCategories(Long warehouseId, Long storeId);

    void createStore(StoreDto storeDto);

    void updateStore(Long id, StoreDto storeDto);

    List<Long> getFinishedGoodsStoreWarehousesIs();
}
