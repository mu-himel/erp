package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "purchase_order_warehouse_details")
public class PurchaseOrderWarehouseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    @JsonIgnore
    private PurchaseOrderDetail purchaseOrderDetail;

    @Column(precision = 38, scale = 4)
    private BigDecimal qty;
    @Column(precision = 38, scale = 4)
    private BigDecimal deliveryCharge;

}
