package com.agi.aesl.erpscm.user_application_validation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class VerifyableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    protected Long id;
    protected String nextVerifierId;
    protected String reviewerId;
    protected String nextApproverId;
    protected LocalDateTime reviewDate;

    @Transient
    @JsonIgnore
    private String status;

    public void setStatus(String status){
        this.status = status;
    }

}
