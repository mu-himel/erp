package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseQuery;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.filter.WarehouseFilterService;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseStoreRepository;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService{


    private final WarehouseRepository warehouseRepository;


    private final CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;


    private final WarehouseStoreRepository warehouseStoreRepository;


    private final IntegrationReaderService integrationReaderService;


    private final WarehouseFilterService warehouseFilterService;


    private final ClaimResolver claimResolver;

    @Override
    @Transactional
    public void createWarehouse(Jwt token, Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByNameAndActive(warehouse.getName(),true);

        if(warehouseOptional.isPresent()){
            throw new AesException("Name already exist");
        }

        warehouseRepository.save(warehouse);

    }

    @Override
    @Transactional
    public void updateWarehouse(Jwt token, Warehouse warehouse) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(warehouse.getId());
        if(warehouseOptional.isEmpty()){
            throw new AesException("Warehouse not found");
        }
        Optional<Warehouse> warehouseExistOptional = warehouseRepository.findByNameAndActive(warehouse.getName(),true);

        if(warehouseExistOptional.isPresent() && !warehouseExistOptional.get().getId().equals(warehouseOptional.get().getId())){
                throw new AesException("Sorry! This warehouse name already exist with different #ID["+warehouseExistOptional.get().getId()+"]");
        }

        Warehouse newWarehouse = new Warehouse(warehouse.getId());
        newWarehouse.setName(warehouse.getName());
        newWarehouse.setLocation(warehouse.getLocation());
        warehouseRepository.save(newWarehouse);
    }

    @Override
    public Page<WarehouseQuery.WarehouseInfo> getWarehouses(Jwt token, Optional<String> name, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String moduleUri="inventory-control/warehouse";

        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(10) );

        DataFilter dataFilter = new DataFilter(moduleUri,claimResolver,pageable);
        dataFilter.setReaderService(integrationReaderService);
        dataFilter.setDataFilterService(warehouseFilterService);
        warehouseFilterService.setName(name.orElse(null));

        Page<WarehouseQuery.WarehouseInfo> filteredData = dataFilter.fetchData();
        if(!filteredData.isEmpty()) {
            return filteredData;
        }

        if(Boolean.TRUE.equals(claimResolver.isAdmin())) {
            return warehouseRepository.findAllByName(name, pageable);
        }

        return Page.empty();
    }

    @Override
    public List<WarehouseQuery.WarehouseInfo> getWarehouses(Jwt token, Optional<String> name) {
        claimResolver.setToken(token);
        String moduleUri="inventory-control/warehouse";
        DataFilter dataFilter = new DataFilter(moduleUri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        return warehouseRepository.findAllByIdsAndName(warehouseIds,name);
    }

    @Override
    @Transactional
    public void deleteWarehouse(Jwt token, Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        Boolean exist = categoryWarehouseStoreRepository.existsByWarehouseId(id);
        Boolean exist1 = warehouseStoreRepository.existsByWarehouseIdAndActive(id,true);
        if(Boolean.TRUE.equals(exist)){
            throw new AesException("Sorry! This warehouse cannot be deleted exist in warehouse referred to category or subcategory ");
        }
        if(Boolean.TRUE.equals(exist1)){
            throw new AesException("Sorry! This warehouse cannot be deleted store exist");
        }
        warehouseOptional.ifPresent(warehouse -> warehouse.setActive(false));
    }

    @Override
    public Optional<Warehouse> getWarehouse(Long id) {
        return warehouseRepository.findWarehouseById(id);
    }

    @Override
    public Optional<Warehouse> getWarehouseByName(String warehouseName) {
        return warehouseRepository.findByName(warehouseName.trim());
    }
    
}
