package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface InventoryRequestService {

    @Deprecated(forRemoval = true)
    Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);

    @Deprecated(forRemoval = true)
    Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getMyProducts(Jwt token, Optional<Integer> page, Optional<Integer> size);


    void createProduct(Jwt token, String uri, ItemRequestDto itemRequestDto);
}
