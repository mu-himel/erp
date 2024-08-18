package com.agi.aesl.erpscm.vendor.service;

import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VendorServiceImpl implements VendorService{

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private IntegrationReaderService integrationReaderService;
    @Override
    public List<?> getAvailableVendors(Jwt token, Optional<String> name) {
        claimResolver.setToken(token);
        return integrationReaderService.getAvailableVendors(name.orElse(null));
    }
}
