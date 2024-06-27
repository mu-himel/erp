package com.agi.aesl.erpscm.user_application_validation.service;

import java.util.Optional;

import com.agi.aesl.erpscm.utils.ClaimResolver;

public interface UserApplicationValidatorService<T> {
    Optional<?> getVerifiers(ClaimResolver claimResolver, String uri,String criteriaGroup, String categories);
}
