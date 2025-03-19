package com.agi.aesl.erpscm.control_panel.inventory_control.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.repository.WarehouseRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryWarehouseStoreRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import static  org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private CategoryWarehouseStoreRepository categoryWarehouseStoreRepository;



    @Test
    void createWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Test Warehouse");

        WarehouseService warehouseService = new WarehouseServiceImpl(warehouseRepository,
                categoryWarehouseStoreRepository,null,
                null,null,null);

        warehouseService.createWarehouse(null,warehouse);
        assertEquals("Test Warehouse",warehouse.getName());
    }
}