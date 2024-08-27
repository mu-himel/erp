package com.agi.aesl.erpscm.user_application_validation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    public String nextVerifierId;
    public String reviewerId;
    public String nextApproverId;
    public LocalDateTime reviewDate;

    @Transient
    private String status;

    public void setStatus(String status){
        this.status = status;
    }

}
