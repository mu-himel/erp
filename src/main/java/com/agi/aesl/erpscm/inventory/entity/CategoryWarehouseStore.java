package com.agi.aesl.erpscm.inventory.entity;


import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;



@Entity
@Data
@Table(name = "scm_category_warehouse_stores")
public class CategoryWarehouseStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne
    private ItemCategory category;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

}
