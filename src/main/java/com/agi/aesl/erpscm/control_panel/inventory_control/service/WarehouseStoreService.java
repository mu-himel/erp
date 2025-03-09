package com.agi.aesl.erpscm.control_panel.inventory_control.service;

// import com.agi.aesl.erpscm.control_panel.inventory_control.dto.CopyToStoreDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.dto.StoreDto;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface WarehouseStoreService {

    void deleteWarehouseStore(Long id);

    Page<?> getStores(Long warehouseId, Optional<Integer> page, Optional<Integer> size);

    List<?> getStoresByWarehouse(Jwt token,Optional<String> name, Optional<Long> warehouseId);
    List<WarehouseStore> getStoresByWarehouseId( Long warehouseId);
    List<WarehouseStore> getStoresByWarehouseIdIn( List<Long> warehouseId);

    Optional<?> getStore(Long warehouseStoreId);
    Optional<WarehouseStore> getStoreById(Long warehouseStoreId);

    // void copyToStore(Long wId, CopyToStoreDto copyToStoreDto);

    Page<?> getFinishedGoodsStoreWarehouses(Optional<String> name, Pageable pageable);

    List<?> getStoreSubCategories(Long warehouseId, Long storeId);

    void createStore(StoreDto storeDto);

    void updateStore(Long id, StoreDto storeDto);

    List<Long> getFinishedGoodsStoreWarehousesIs();
}
