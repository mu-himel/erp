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

    private BigDecimal qty;

    private BigDecimal currentStock;

    private BigDecimal safetyStock;

    private BigDecimal transferQty;

    private BigDecimal inTransit;
    private BigDecimal inTransitReturn;

    private BigDecimal receivedQty;

    private Boolean isDecline;

    private String receiveNote;
    private String declineNote;

    @ManyToOne
    @JsonIgnore
    private InternalRequisitionDetail internalRequisitionDetail;
}
