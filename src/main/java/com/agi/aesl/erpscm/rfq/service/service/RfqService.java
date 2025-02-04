package com.agi.aesl.erpscm.rfq.service.service;

import com.agi.aesl.erpscm.rfq.dto.RfqRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface RfqService {
    Page<?> getAllPendingRFQs(Jwt token, Optional<String> indentNo, Optional<Long> category,
                              Optional<Long> subCategory, Optional<String> priority,
                              Optional<Integer> daysRemain, Optional<String> fromDate,
                              Optional<String> toDate, Optional<Integer> page, Optional<Integer> size);

    Page<?> getAllSentRfqs(Jwt token, Optional<String> indentNo, Optional<Long> category,
                           Optional<Long> subCategory, Optional<String> priority, Optional<Integer> daysRemain,
                           Optional<String> fromDate, Optional<String> toDate,
                           Optional<Integer> page, Optional<Integer> size);

    Optional<?> getAvailableVendorsCount(Jwt token, Long id);

    void createRfq(Jwt token, RfqRequestDto requestDto);

    public Page<?> getAllClosedRFQs(Jwt token, Optional<String> indentNo, Optional<String> category,
                                    Optional<String> subCategory, Optional<String> priority,
                                    Optional<Integer> daysRemain, Optional<String> fromDateOp,
                                    Optional<String> toDateOp, Optional<Integer> page, Optional<Integer> size);
    void expire(Jwt token, Long id);
}
