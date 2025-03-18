package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.entity.ItemStock;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.agi.aesl.erpscm.inventory.repository.ItemRepository;
import com.agi.aesl.erpscm.inventory.repository.ItemStockRepository;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Setter
@Service

public class ItemStockService {

    ItemStockRepository itemStockRepository;

    @Transactional
    public void updateStock(Item item,BigDecimal qty, StockType stockType, Long warehouseId, Long warehouseStoreId){

        ItemStock itemStock = new ItemStock(qty, item,stockType,new Warehouse(warehouseId),new WarehouseStore(warehouseStoreId));
        itemStockRepository.save(itemStock);
    }
}
