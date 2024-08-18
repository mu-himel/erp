package com.agi.aesl.erpscm.quality_control.entity;

import com.agi.aesl.erpscm.goods_receive.enums.QcType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "quality_control_kpis")
public class QualityControlKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private QcType qcType;

    @Column(length = 500)
    private String remark;

    @ManyToOne
    @JsonIgnore
    private QualityControl qualityControl;
}
