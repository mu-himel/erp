package com.agi.aesl.erpscm.control_panel.inventory_control.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scm_warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name is Required")
    @NotBlank(message = "Name is Required")
    private String name;

    @OneToMany(mappedBy = "warehouse",cascade = CascadeType.ALL)
    private List<WarehouseStore> warehouseStores=new ArrayList<>();

    @Column(length = 500)
    private String location;

    private Boolean active = true;

    public Warehouse(Long id) {
        this.id = id;
    }
}
