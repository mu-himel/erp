package com.agi.aesl.erpscm.vendor.service;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface VendorService {
    List<?> getAvailableVendors(Jwt token, Optional<String> name);
}
