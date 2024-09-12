package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface InventoryCategoryRequestService {
    void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto);

    Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);
    Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);
}
