package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.network.NetworkService;

@Service
public class IntegrationReaderServiceImpl implements IntegrationReaderService{

    @Autowired
    private NetworkService networkService;

    @Value("${acl.apiEndpoint}")
    private String aclApiEndpoint;

    @Override
    public Optional<Map<String, List<Long>>> getModuleFilterByUri(Jwt token, String uri) {
        HttpHeaders headers = networkService.setHttpHeaders(token);
        headers.set("uri", uri);
        HttpEntity<?> payload = new HttpEntity<>(headers);
        String url = aclApiEndpoint+"/modules/filter-by-uri";
        System.out.println(url);
        ResponseEntity<?> response = networkService.get(url,payload, Map.class);
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
        System.out.println(response.getBody());

        return (response.getBody()==null)? Optional.empty(): Optional.ofNullable((Map<String, List<Long>>)response.getBody());
    }
    
}
