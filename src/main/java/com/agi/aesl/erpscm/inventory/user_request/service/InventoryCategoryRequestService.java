package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryCategoryRequestService extends VerificationDomainService {
    void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto);

    Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);
    Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingVerifications(Jwt token, Optional<Integer> page, Optional<Integer> size, Boolean isCategory);

    Page<?> getPendingApprovals(Jwt token, Optional<Integer> page, Optional<Integer> size, boolean isCategory);
    Page<?> getClosed(Jwt token, Optional<Integer> page, Optional<Integer> size, boolean isCategory);

    Map<String,Object> getDetail(Long id);

    void review(Jwt token, Long id, ReviewDto reviewDto);
}
