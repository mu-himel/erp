package com.agi.aesl.erpscm.cs.entity;

import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.user_application_validation.entity.VerifyableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cs_accounts")
public class CsAccount extends VerifyableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Cs cs;

    private String csType;
    private String vatType;
    private String deliveryValuationMethod;

    @Enumerated(EnumType.STRING)
    private CsStatus acsStatus;

    @Enumerated(EnumType.STRING)
    private CsStatus reviewPrevStatus;

    @ManyToOne
    private Employee requestedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Override
    public void setStatus(String status){
        this.acsStatus = CsStatus.valueOf(status);
    }

}
