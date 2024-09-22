package com.agi.aesl.erpscm.purchase_order.entity;

import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.purchase_order.service.PurchaseOrderService;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "cs_po")
@EqualsAndHashCode(callSuper = true)
public class PoGroup extends VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Cs cs;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus purchaseOrderStatus;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus reviewPrevStatus;

    @Column(length = 500)
    private String declineNote;

    private Boolean isVerifyApproveEnabled;

}
