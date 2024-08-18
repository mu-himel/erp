package com.agi.aesl.erpscm.inventory.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.inventory.enums.ItemInactiveStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scm_item_import_logs")
public class ItemImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Item item;

    @ManyToOne
    @JsonIgnore
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private ItemInactiveStatus itemInactiveStatus;

}
