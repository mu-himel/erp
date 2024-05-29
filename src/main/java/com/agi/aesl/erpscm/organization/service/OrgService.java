package com.agi.aesl.erpscm.organization.service;

import java.util.Optional;

import com.agi.aesl.erpscm.organization.dto.request.OrgRequestDto;
import com.agi.aesl.erpscm.organization.entity.Organization;

public interface OrgService {
    
    void createOrg(OrgRequestDto orgRequestDto);

    Optional<Organization> getOrgByCode(String orgCode);
    Optional<Organization> getOrgByCodeFromAcl(String token);
}
