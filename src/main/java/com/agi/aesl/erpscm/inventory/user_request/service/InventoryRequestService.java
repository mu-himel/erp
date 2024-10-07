package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public interface InventoryRequestService extends VerificationDomainService {



    Page<?> getMyProducts(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                          Optional<Integer> page, Optional<Integer> size);


    void createProduct(Jwt token, String uri, UserItemRequestDto itemRequestDto);

    Map<String,Object> getDetail(Long id);

    Page<?> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                    Optional<Integer> page, Optional<Integer> size);
    Page<?> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                Optional<Integer> page, Optional<Integer> size);
    Page<?> getClosed(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                      Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingApprovalItemsByStore(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                      Optional<Integer> page, Optional<Integer> size);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void approveByStore(Jwt token, Long id);
    void rejectByStore(Long id);
}
