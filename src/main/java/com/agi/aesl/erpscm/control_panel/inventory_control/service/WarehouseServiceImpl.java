package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;

@Service
public class WarehouseServiceImpl implements WarehouseService{

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;

    @Autowired
    private WarehouseStoreRepository warehouseStoreRepository;


    @Override
    public void createWarehouse(Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByName(warehouse.getName());

        if(warehouseOptional.isPresent()){
            throw new AesException("Name already exist");
        }

        // if(warehouse.getWarehouseStores().size()==0){
        //     throw new AesException("Please Select Store");
        // }

        warehouse.setWarehouseStores(warehouse.getWarehouseStores().stream().map(warehouseStore -> {
            warehouseStore.setId(null);

            warehouseStore.setAlias(warehouse.getName().toLowerCase()+"-"
                                +warehouseStore.getStoreName().toLowerCase());

            warehouseStore.setWarehouse(warehouse);
            return warehouseStore;
        }).collect(Collectors.toList()));
        warehouseRepository.save(warehouse);
    }

    @Override
    public void updateWarehouse(Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(warehouse.getId());
        if(warehouseOptional.isEmpty()){
            throw new AesException("Warehouse not found");
        }
        warehouse.setWarehouseStores(warehouse.getWarehouseStores().stream().map(warehouseStore -> {
            warehouseStore.setWarehouse(warehouse);
            return warehouseStore;
        }).collect(Collectors.toList()));
        warehouseRepository.save(warehouse);
    }

    @Override
    public Page<?> getWarehouses(Optional<String> name, Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10) );
        return warehouseRepository.findAllByName(name,pageable);
    }

    @Override
    public List<?> getWarehouses(Optional<String> name) {
        return warehouseRepository.findAllByName(name);
    }

    @Override
    public void deleteWarehouse(Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        Boolean exist = categoryWarehouseStoreRepository.existsByWarehouseId(id);
        Boolean exist1 = warehouseStoreRepository.existsByWarehouseIdAndActive(id,true);
        if(exist || exist1){
            throw new AesException("Sorry! This warehouse cannot be deleted");
        }
        warehouseOptional.ifPresent(warehouse -> warehouse.setActive(false));
    }

    @Override
    public Optional<Warehouse> getWarehouse(Long id) {
        Optional<Warehouse> ws = warehouseRepository.findWarehouseById(id);
        return ws;
    }

    @Override
    public Optional<Warehouse> getWarehouseByName(String warehouseName) {
        return warehouseRepository.findByName(warehouseName);
    }
    
}
