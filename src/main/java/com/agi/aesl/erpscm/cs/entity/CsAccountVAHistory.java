package com.agi.aesl.erpscm.cs.entity;

import com.agi.aesl.erpscm.cs.enums.CsStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cs_account_va_histories")
public class CsAccountVAHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CsAccount csAccount;

    @ManyToOne
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private CsStatus acsStatus;

    @CreationTimestamp
    private LocalDateTime verificationDate;
}
