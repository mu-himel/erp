package com.agi.aesl.erpscm.user_application_validation.entity;

import java.time.LocalDateTime;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.employee.entity.Employee;

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
@Table(name = "user_application_validations")
public class UserApplicationValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long domainId;
    @Enumerated(EnumType.STRING)
    private DomainType domainType;
    
    @ManyToOne
    private Employee verifier;
    private Boolean verified=false;
    private Boolean isApproval=false;
    private LocalDateTime verificationDate;
}
