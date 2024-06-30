package com.agi.aesl.erpscm.modules.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.modules.dto.VerifierConfig;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.user_application_validation.dto.response.ApprovalPanel;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        httpHeaders.set("uri",uri);
        String url = aclAPIEndpoint+"/verifiers?criteriaGroup="+criteriaGroup+"&categories="+criteriaValues;
        HttpEntity<?> payload = new HttpEntity<>(httpHeaders);
        ResponseEntity<VerifierConfig> response = networkService.get(url, payload, VerifierConfig.class);
        
        System.out.println(response.getBody());
        return Optional.ofNullable(response.getBody());
    }

    @Override
    public List<ApprovalPanel> getModuleWiseApprovalSetting(ClaimResolver claimResolver, String uri, Optional<String> categoryId,
            Optional<BigDecimal> amount) {
                HttpHeaders httpHeaders = networkService.setHttpHeaders(claimResolver.getToken());
                httpHeaders.set("uri", uri);
                String url = aclAPIEndpoint+"/approval-settings?categoryId="+categoryId.get();
                HttpEntity<?> payload = new HttpEntity<>(httpHeaders);
                ResponseEntity<String> response = (ResponseEntity<String>) networkService.get(url, payload, String.class);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<ApprovalPanel> panels = mapper.readValue(response.getBody(), new TypeReference<List<ApprovalPanel>>() {
                        
                    });
                    return panels;
                } catch (JsonProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println(response.getBody());
                return new ArrayList<>();
    }

    
    
}
