package com.agi.aesl.erpscm.modules.service;

import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.modules.dto.ModuleInfo;
import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.utils.ClaimResolver;

public interface ModuleService {
    Optional<?> getVerifierConfigByModuleAndCriteriaGroup(ClaimResolver claimResolver, String uri, String criteriaGroup, String criteriaValues);
}
