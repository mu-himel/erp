package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryApproveDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryCategoryRequestService extends VerificationDomainService {
    void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto);

    List<?> getCategories(Jwt token, Optional<String> name, Optional<String> code,Optional<Long>warehouseId,Optional<Long>storeId);
    List<?> getSubCategories(Jwt token, Long categoryId, Optional<String> name, Optional<String> code);

    Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);
    Page<?> getMySubCategories(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, Boolean isCategory);

    Page<?> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory);
    Page<?> getClosed(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory);
    Page<?> getPendingApprovalCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                                  Optional<String> name,
                                                  Optional<Long> warehouseId,
                                                  Optional<Long> warehouseStoreId,
                                                  Optional<Integer> page, Optional<Integer> size,
                                                  boolean isCategory);
    Page<?> getPendingApprovalSubCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                             Optional<String> name,
                             Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                             Optional<Integer> page, Optional<Integer> size, boolean isCategory);

    Map<String,Object> getDetail(Long id);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void approveByStore(Jwt token, Long id, CategoryApproveDto approveDto);

    void rejectByStore(Long id, CategoryRejectDto noteDto);
}
