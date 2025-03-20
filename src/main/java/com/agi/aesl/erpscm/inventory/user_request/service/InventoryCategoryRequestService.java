package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryApproveDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InventoryCategoryRequestService extends VerificationDomainService {
    void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto);

    List<UserCategoryRepository.UserCategoryInfo> getCategories(Jwt token, Optional<String> name, Optional<String> code, Optional<Long>warehouseId, Optional<Long>storeId);
    List<UserCategoryRepository.UserCategoryInfo> getSubCategories(Jwt token, Long categoryId, Optional<String> name, Optional<String> code);

    Page<UserCategoryRepository.UserCategory> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size);
    Page<UserCategoryRepository.UserSubCategory> getMySubCategories(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size);

    <T> T getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, Boolean isCategory);

    <T> T getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory);
    <T> T getClosed(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory);
    Page<UserCategoryRepository.PendingApprovalStore> getPendingApprovalCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                                                                            Optional<String> name,
                                                                                            Optional<Long> warehouseId,
                                                                                            Optional<Long> warehouseStoreId,
                                                                                            Optional<Integer> page, Optional<Integer> size,
                                                                                            boolean isCategory);
    Page<UserCategoryRepository.PendingApprovalStore> getPendingApprovalSubCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                                                                               Optional<String> name,
                                                                                               Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                                                                               Optional<Integer> page, Optional<Integer> size, boolean isCategory);

    Map<String,Object> getDetail(Long id);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void approveByStore(Jwt token, Long id, CategoryApproveDto approveDto);

    void rejectByStore(Long id, CategoryRejectDto noteDto);
}
