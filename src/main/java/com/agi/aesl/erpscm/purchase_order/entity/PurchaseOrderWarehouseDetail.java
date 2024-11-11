package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
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
    private PurchaseOrderDetail purchaseOrderDetail;

    private BigDecimal qty;

}
