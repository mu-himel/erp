package com.agi.aesl.erpscm.control_panel.inventory_control.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scm_warehouse_stores")
public class WarehouseStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private String storeName;

    // name of warehouse and store concat with _ and lowercase
    private String alias;


    @ManyToOne
    @JsonIgnore
    private Warehouse warehouse;

    private Boolean active = true;

    public WarehouseStore(Long id) {
        this.id = id;
    }
}
