package com.agi.aesl.erpscm.internal_requisition.entity;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.internal_requisition.enums.IrStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "store_irs")
public class StoreIR extends VerifyableEntity {

    @ManyToOne
    private InternalRequisition ir;

    @Enumerated(EnumType.STRING)
    private IrStatus irStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    private Employee requestedBy;

    @Override
    public void setStatus(String status) {
        this.irStatus = IrStatus.valueOf(status);
    }
}
