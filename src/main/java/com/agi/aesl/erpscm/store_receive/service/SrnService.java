package com.agi.aesl.erpscm.store_receive.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.store_receive.dto.SrnDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface SrnService extends VerificationDomainService {
    void addSrn(Jwt token, SrnDto srnDto);

    List<?> getPendingDemandListBySrnItems(Long id);
    List<?> getPendingDemandListBySrnItems(String attributes);

    Page<?> getAll(Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate, Optional<String> toDate);
    Page<?> getAllComplete(Optional<Integer> page, Optional<Integer> size, Optional<String> fromDate,
                           Optional<String> toDate);

    void review(Jwt token, Long id, ReviewDto reviewDto);
}
