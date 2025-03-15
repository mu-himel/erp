package com.agi.aesl.erpscm.vendor.service;

import com.agi.aesl.erpscm.erpn_integration.dto.response.VendorListInfo;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService{


    private final ClaimResolver claimResolver;


    private final IntegrationReaderService integrationReaderService;
    @Override
    public List<VendorListInfo> getAvailableVendors(Jwt token, Optional<String> name) {
        claimResolver.setToken(token);
        return integrationReaderService.getAvailableVendors(name.orElse(null));
    }
}
