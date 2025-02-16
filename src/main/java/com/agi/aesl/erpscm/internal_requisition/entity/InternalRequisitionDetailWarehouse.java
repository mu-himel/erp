package com.agi.aesl.erpscm.internal_requisition.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "internal_requisition_warehouses")
public class InternalRequisitionDetailWarehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Warehouse fromWarehouse;

    @ManyToOne
    private Warehouse toWarehouse;
    @Column(precision = 38, scale = 4)
    private BigDecimal qty;
    @Column(precision = 38, scale = 4)
    private BigDecimal currentStock;

    @Column(precision = 38, scale = 4)
    private BigDecimal safetyStock;

    @Column(precision = 38, scale = 4)
    private BigDecimal transferQty;
    @Column(precision = 38, scale = 4)
    private BigDecimal inTransit;
    @Column(precision = 38, scale = 4)
    private BigDecimal inTransitReturn;

    @Column(precision = 38, scale = 4)
    private BigDecimal receivedQty;

    private Boolean isDecline;

    private String receiveNote;
    private String declineNote;

    @ManyToOne
    @JsonIgnore
    private InternalRequisitionDetail internalRequisitionDetail;
}
