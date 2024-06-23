package com.agi.aesl.erpscm.erpn_integration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;

public interface IntegrationReaderService {

    Optional<Map<String,List<Long>>> getModuleFilterByUri(Jwt token, String uri);
}
