package com.agi.aesl.erpscm.cs.service;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface CsService {
    Page<?> getAllPendingCs(Jwt token, Optional<Integer> page, Optional<Integer> size);
}
