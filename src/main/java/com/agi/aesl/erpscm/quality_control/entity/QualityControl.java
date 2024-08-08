package com.agi.aesl.erpscm.quality_control.entity;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.quality_control.enums.QcStatus;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "quality_controls")
public class QualityControl extends VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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


    private String nextVerifierId;
    private String nextApproverId;

    @Enumerated(EnumType.STRING)
    private QcStatus reviewPrevStatus;

    private String reviewerId;
    private LocalDateTime reviewDate;


}
