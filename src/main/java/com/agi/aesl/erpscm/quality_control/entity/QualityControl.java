package com.agi.aesl.erpscm.quality_control.entity;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "quality_controls")
public class QualityControl extends VerifyableEntity {



    @ManyToOne
    private GoodReceiveNote goodReceiveNote;

    @OneToMany(mappedBy = "qualityControl", cascade = CascadeType.ALL)
    private List<QualityControlKpi> qualityControlKpis;

    private LocalDate qcDate;

    @ManyToOne
    private Warehouse warehouse;

    @Column(length = 500)
    private String comment;

    @Enumerated(EnumType.STRING)
    private QcStatus qcStatus;

    @ManyToOne
    private Employee createdBy;


    @Enumerated(EnumType.STRING)
    private QcStatus reviewPrevStatus;



}
