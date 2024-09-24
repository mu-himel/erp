package com.agi.aesl.erpscm.cs.entity;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "cs")
@Data
public class Cs extends VerifyableEntity {

    private Long id;

    @ManyToOne
    private Indent indent;

    @ManyToOne
    private Employee requestedBy;

    private String declineNote;

    @Enumerated(EnumType.STRING)
    private CsStatus csStatus;

    @Enumerated(EnumType.STRING)
    private CsStatus reviewPrevStatus;

    @OneToMany(mappedBy = "cs", cascade = CascadeType.ALL)
    private List<CsDetail> csDetails;

    private String csNo;
    private BigDecimal vatAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalPrice;
    private BigDecimal subTotalPrice;

    @ManyToOne
    private Warehouse warehouse;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public void setStatus(String status){
        this.csStatus = CsStatus.valueOf(status);
    }

}
