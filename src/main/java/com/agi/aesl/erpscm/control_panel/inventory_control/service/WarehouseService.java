package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;

public interface WarehouseService {
    void createWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);

    Page<?> getWarehouses(Optional<String> name, Optional<Integer> page, Optional<Integer> size);

    List<?> getWarehouses(Optional<String> name);


    void deleteWarehouse(Long id);

    Optional<Warehouse> getWarehouse(Long id);
    Optional<Warehouse> getWarehouseByName(String warehouseName);
}
