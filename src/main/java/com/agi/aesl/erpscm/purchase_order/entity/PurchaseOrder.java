package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cs cs;

    @ManyToOne
    private PoGroup poGroup;

    private LocalDate poDate;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    private String poNo;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<PurchaseOrderDetail> purchaseOrderDetails;

    @ManyToOne
    private Employee requestedBy;
}
