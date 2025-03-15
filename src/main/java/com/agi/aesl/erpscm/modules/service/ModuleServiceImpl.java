package com.agi.aesl.erpscm.modules.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.modules.dto.UserAssignInfo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService{

    private final NetworkService networkService;

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

        return Optional.ofNullable(response.getBody());
    }

    @Override
    @Transactional
    public List<ApprovalPanel> getModuleWiseApprovalSetting(ClaimResolver claimResolver, String uri, Optional<String> categoryId,
            Optional<BigDecimal> amount) {

                HttpHeaders httpHeaders = networkService.setHttpHeaders(claimResolver.getToken());
                httpHeaders.set("uri", uri);
                String catId = categoryId.orElse("");
                String url = aclAPIEndpoint+"/approval-settings?categories="+catId;
                HttpEntity<?> payload = new HttpEntity<>(httpHeaders);
                ResponseEntity<String> response =  networkService.get(url, payload, String.class);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(response.getBody(),
                            new TypeReference<List<ApprovalPanel>>() {});
                } catch (JsonProcessingException e) {
                    throw new AesException(e.getMessage());
                }
    }

    @Override
    public List<UserAssignInfo> getUsersByPermission(ClaimResolver claimResolver, String uri) {
        HttpHeaders httpHeaders = networkService.setHttpHeaders(claimResolver.getToken());
        httpHeaders.set("uri", uri);
        String url = aclAPIEndpoint+"/modules/permissions/get-users-by-permission";
        HttpEntity<?> payload = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = networkService.get(url, payload, String.class);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(),
                    new TypeReference<List<UserAssignInfo>>() {});
        } catch (JsonProcessingException e) {
            throw new AesException(e.getMessage());
        }
    }
}
