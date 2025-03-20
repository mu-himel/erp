package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserItemRepository;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public interface InventoryRequestService extends VerificationDomainService {



    Page<UserItemRepository.UserItem> getMyProducts(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                    Optional<Integer> page, Optional<Integer> size);


    void createProduct(Jwt token, String uri, UserItemRequestDto itemRequestDto);

    Map<String,Object> getDetail(Long id);

    Page<UserItemRepository.UserItem> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                              Optional<Integer> page, Optional<Integer> size);
    Page<UserItemRepository.UserItem> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                Optional<Integer> page, Optional<Integer> size);
    Page<UserItemRepository.UserItem> getClosed(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                Optional<Integer> page, Optional<Integer> size);

    Page<UserItemRepository.PendingApprovalUserItem> getPendingApprovalItemsByStore(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                                                    Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                                                                    Optional<Integer> page, Optional<Integer> size);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    void approveByStore(Jwt token, Long id);
    void rejectByStore(Long id, CategoryRejectDto rejectDto);
}
