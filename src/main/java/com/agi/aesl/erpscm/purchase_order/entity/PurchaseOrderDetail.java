package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.cs.entity.CsVendorDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @Column(precision = 38, scale = 4)
    private BigDecimal unitPrice;
    @Column(precision = 38, scale = 4)
    private BigDecimal deliveryQty;
    @Column(precision = 38, scale = 4)
    private BigDecimal remainingQty;


    private LocalDate deliveryDate;

    private String transactionType;
    private String estimatedDeliveryDays;
    private String creditDays;
    @Column(precision = 38, scale = 4)
    private BigDecimal totalPrice;
    private String warrantyUnit;
    private String warrantyDuration;
    @Column(precision = 38, scale = 4)
    private BigDecimal vatPercent;
    @Column(precision = 38, scale = 4)
    private BigDecimal vatAmount;
    @Column(precision = 38, scale = 4)
    private BigDecimal deliveryCharge;
    @Column(precision = 38, scale = 4)
    private BigDecimal subTotal;

    @ManyToOne
    private Warehouse warehouse;

    @OneToMany(mappedBy = "purchaseOrderDetail", cascade = CascadeType.ALL)
    private List<PurchaseOrderWarehouseDetail> warehouseDetailList;
}
