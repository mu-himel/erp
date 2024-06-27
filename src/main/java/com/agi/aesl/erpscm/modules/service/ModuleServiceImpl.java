package com.agi.aesl.erpscm.modules.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.utils.ClaimResolver;

@Service
public class ModuleServiceImpl implements ModuleService{

    @Autowired
    private NetworkService networkService;

    @Value("${acl.apiEndpoint}")
    private String aclAPIEndpoint;

    @Override
    public Optional<VerifierConfig> getVerifierConfigByModuleAndCriteriaGroup(ClaimResolver claimResolver, String uri,
            String criteriaGroup, String criteriaValues) {
        
        HttpHeaders httpHeaders = networkService.setHttpHeaders(claimResolver.getToken());
        String url = aclAPIEndpoint+"/verifiers";
        HttpEntity<?> payload = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = (ResponseEntity<String>) networkService.get(url, payload, String.class);
                
        return null;
    }
    
}
