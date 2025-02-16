package com.agi.aesl.erpscm.store_receive.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "store_receive_details")
public class StoreReceiveDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private StoreReceiveNote storeReceiveNote;

    @OneToOne
    private GoodReceiveItemDetail goodReceiveItemDetail;

    @ManyToOne
    private Item item;

    @Column(precision = 38, scale = 4)
    private BigDecimal stockInQty;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

    private String costCenter;
}
