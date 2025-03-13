package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseQuery;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;

public interface WarehouseService {
    void createWarehouse(Jwt token, Warehouse warehouse);
    void updateWarehouse(Jwt token,  Warehouse warehouse);

    Page<WarehouseQuery.WarehouseInfo> getWarehouses(Jwt token, Optional<String> name, Optional<Integer> page, Optional<Integer> size);

    List<WarehouseQuery.WarehouseInfo> getWarehouses(Jwt token, Optional<String> name);


    void deleteWarehouse(Jwt token, Long id);

    Optional<Warehouse> getWarehouse(Long id);
    Optional<Warehouse> getWarehouseByName(String warehouseName);
}
