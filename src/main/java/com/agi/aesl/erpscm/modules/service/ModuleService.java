package com.agi.aesl.erpscm.modules.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.utils.ClaimResolver;

public interface ModuleService {
    Optional<VerifierConfig> getVerifierConfigByModuleAndCriteriaGroup(ClaimResolver claimResolver, String uri, String criteriaGroup, String criteriaValues);
    List<ApprovalPanel> getModuleWiseApprovalSetting(ClaimResolver claimResolver,String uri,Optional<String> categoryId, Optional<BigDecimal> amount);
}
