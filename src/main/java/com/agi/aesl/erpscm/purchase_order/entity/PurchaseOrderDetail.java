package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "purchase_order_details")
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    private CsVendorDetail csVendorDetail;

    private String itemName;

    private BigDecimal unitPrice;
    private BigDecimal deliveryQty;

    private LocalDate deliveryDate;

    @ManyToOne
    private Warehouse warehouse;
}
