package com.agi.aesl.erpscm.purchase_order.dto.request;

import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "po_terms_conditions")
public class PoTermsCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private PurchaseOrder purchaseOrder;

    private String tnc;
}
