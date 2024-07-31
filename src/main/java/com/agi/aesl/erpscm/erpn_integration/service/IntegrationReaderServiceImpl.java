package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import aj.org.objectweb.asm.TypeReference;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.erpn_integration.dto.response.VendorListInfo;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.network.NetworkService;

@Service
public class IntegrationReaderServiceImpl implements IntegrationReaderService{

    @Autowired
    private NetworkService networkService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private CpsServerConfig cpsServerConfig;

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

    @Override
    public List<VendorListInfo> getAvailableVendors(String name) {
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<?> payload = new HttpEntity<>(headers);
        ResponseEntity<?> response = networkService.get(
                cpsServerConfig.getVendorListEndpoint(name),
                payload,
                List.class
        );
        if(response.getStatusCode().equals(HttpStatus.OK)){
            return (List<VendorListInfo>) response.getBody();
        }
        return  new ArrayList<>();
    }
}
