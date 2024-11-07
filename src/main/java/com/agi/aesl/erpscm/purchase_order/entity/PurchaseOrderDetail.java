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
    private BigDecimal remainingQty;

    private LocalDate deliveryDate;

    private String transactionType;
    private String estimatedDeliveryDays;
    private String creditDays;
    private BigDecimal totalPrice;
    private String warrantyUnit;
    private String warrantyDuration;
    private BigDecimal vatPercent;
    private BigDecimal vatAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal subTotal;

    @ManyToOne
    private Warehouse warehouse;
}
