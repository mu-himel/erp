package com.agi.aesl.erpscm.user_application_validation.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Data

public abstract class VerifyableEntity {
    protected Long id;
    protected String nextVerifierId;
}
