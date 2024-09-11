package com.agi.aesl.erpscm.inventory.user_request.service;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface InventoryRequestService {

    Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getMyProducts(Jwt token, Optional<Integer> page, Optional<Integer> size);
}
