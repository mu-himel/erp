package com.agi.aesl.erpscm.rfq.controller.service;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface RfqService {
    Page<?> getAllPendingRFQs(Jwt token, Optional<String> indentNo, Optional<String> category,
                              Optional<String> subCategory, Optional<String> priority,
                              Optional<Integer> daysRemain, Optional<String> fromDate,
                              Optional<String> toDate, Optional<Integer> page, Optional<Integer> size);

    Page<?> getAllSentRfqs(Jwt token, Optional<String> indentNo, Optional<String> category,
                           Optional<String> subCategory, Optional<String> priority, Optional<Integer> daysRemain,
                           Optional<String> fromDate, Optional<String> toDate,
                           Optional<Integer> page, Optional<Integer> size);
}
