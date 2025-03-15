package com.agi.aesl.erpscm.organization.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.organization.dto.request.OrgRequestDto;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.repository.OrgRepository;
import com.agi.aesl.erpscm.network.NetworkService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService{


    private final OrgRepository orgRepository;


    private final NetworkService networkService;


    private final CpsServerConfig cpsServerConfig;

    @Value("${acl.apiEndpoint}")
    private String aclApiEndpoint;

    @Override
    public void createOrg(OrgRequestDto orgRequestDto) {
        Organization org = new Organization();
        Optional<Organization> orgOp = orgRepository.findByOrgCode(cpsServerConfig.getOrgCode());
        if(orgOp.isPresent()){
            return;
        }
        org.setOrgName(orgRequestDto.getName());
        org.setOrgCode(orgRequestDto.getCode());
        orgRequestDto.setServiceIpAddress(cpsServerConfig.getErpIpAddress());
        orgRequestDto.setServiceUsername("superadmin@gmail.com");
        orgRequestDto.setServicePassword("12345678");
        Optional<Long> cpsOrgRegId = createOrgInCps(orgRequestDto);
        if(!cpsOrgRegId.isEmpty()){
            org.setCpsVendorRegistrationId(cpsOrgRegId.get());
            orgRepository.save(org);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<Long> createOrgInCps(OrgRequestDto orgRequestDto){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrgRequestDto> payload = new HttpEntity<>(orgRequestDto, headers);
        try{
            String url = cpsServerConfig.getOrgRegisterEndpoint();
            ResponseEntity<Void> response = networkService.post(url, payload, Void.class);
            if(response.getStatusCode().equals(HttpStatus.CREATED)){
                String orgId = response.getHeaders().get("orgId").get(0);
                if(orgId != null && !orgId.isEmpty()){
                    return Optional.of(Long.parseLong(orgId));
                }
            }
            return Optional.empty();
        }catch(Exception ex){
            log.error(ex.getLocalizedMessage());
        }

        return Optional.empty();
        
    }

    @Override
    public Optional<Organization> getOrgByCode(String orgCode) {
        return orgRepository.findByOrgCode(orgCode);
    }

    @Override
    public Optional<Organization> getOrgByCodeFromAcl(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<?> payload = new HttpEntity<>(headers);
        try{
            String url = aclApiEndpoint.concat("/organization");
            log.info("Token get from: "+ url);
            log.info("TOKEN: "+token);
            ResponseEntity<Organization> response = networkService.get(url, payload, Organization.class);
            return Optional.ofNullable(response.getBody());
        }catch(Exception ex){
            log.error(ex.getLocalizedMessage());
        }
        return Optional.empty();
    }

    

    
    
}
