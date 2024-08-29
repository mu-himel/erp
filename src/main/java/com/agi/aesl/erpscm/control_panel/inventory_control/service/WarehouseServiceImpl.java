package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.filter.WarehouseFilterService;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationWriterService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;

import jakarta.transaction.Transactional;

@Service
public class WarehouseServiceImpl implements WarehouseService{

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;

    @Autowired
    private WarehouseStoreRepository warehouseStoreRepository;

    @Autowired
    private IntegrationWriterService integrationWriterService;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private WarehouseFilterService warehouseFilterService;

    @Autowired
    private ClaimResolver claimResolver;


    @Override
    @Transactional
    public void createWarehouse(Jwt token, Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByNameAndActive(warehouse.getName(),true);

        if(warehouseOptional.isPresent()){
            throw new AesException("Name already exist");
        }


        warehouseRepository.save(warehouse);

        if(warehouse.getId()!=null){
            integrationWriterService.createWarehouse(token, warehouse);
        }

    }

    @Override
    @Transactional
    public void updateWarehouse(Jwt token, Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(warehouse.getId());
        if(warehouseOptional.isEmpty()){
            throw new AesException("Warehouse not found");
        }
        Optional<Warehouse> warehouseExistOptional = warehouseRepository.findByNameAndActive(warehouse.getName(),true);

        if(warehouseExistOptional.isPresent()){
            if(!warehouseExistOptional.get().getId().equals(warehouseOptional.get().getId())){
                throw new RuntimeException("Sorry! This warehouse name already exist with different #ID["+warehouseExistOptional.get().getId()+"]");
            }
//            throw new AesException("Name already exist");
        }

        String oldName = warehouseOptional.get().getName();
        Warehouse newWarehouse = new Warehouse(warehouse.getId());
        newWarehouse.setName(warehouse.getName());
        newWarehouse.setLocation(warehouse.getLocation());
        // warehouse.setWarehouseStores(warehouse.getWarehouseStores().stream().map(warehouseStore -> {
        //     warehouseStore.setWarehouse(warehouse);
        //     return warehouseStore;
        // }).collect(Collectors.toList()));
        warehouseRepository.save(newWarehouse);
        if(newWarehouse.getId()!=null){
            
            integrationWriterService.updateWarehouse(token,oldName, newWarehouse);
        }
    }

    @Override
    public Page<?> getWarehouses(Jwt token, Optional<String> name, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String moduleUri="inventory-control/warehouse";

        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10) );

        DataFilter dataFilter = new DataFilter(moduleUri,claimResolver,pageable);
        dataFilter.setReaderService(integrationReaderService);
        dataFilter.setDataFilterService(warehouseFilterService);
        warehouseFilterService.setName(name.orElse(null));

        Page<?> filteredData = dataFilter.fetchData();
        if(!filteredData.isEmpty()) {
            return filteredData;
        }

        if(claimResolver.isAdmin()) {
            return warehouseRepository.findAllByName(name, pageable);
        }

        return Page.empty();
    }

    @Override
    public List<?> getWarehouses(Optional<String> name) {
        return warehouseRepository.findAllByName(name);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Jwt token, Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        Boolean exist = categoryWarehouseStoreRepository.existsByWarehouseId(id);
        Boolean exist1 = warehouseStoreRepository.existsByWarehouseIdAndActive(id,true);
        if(exist || exist1){
            throw new AesException("Sorry! This warehouse cannot be deleted");
        }
        warehouseOptional.ifPresent(warehouse -> {
            warehouse.setActive(false);
            integrationWriterService.deleteWarehouse(token, warehouse.getName());
        });
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
