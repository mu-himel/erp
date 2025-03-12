package com.agi.aesl.erpscm.demand.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.agi.aesl.erpscm.demand.enums.DemandStatus;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "scm_demand_verification_approval_histories")
public class DemandVerificationApprovalHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Demand demand;

    @ManyToOne
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private DemandStatus demandStatus;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime verificationDate;
}
