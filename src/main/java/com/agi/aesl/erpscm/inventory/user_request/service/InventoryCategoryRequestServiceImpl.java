package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryAttribute;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryBrand;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryCategoryRequestServiceImpl implements InventoryCategoryRequestService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private UserCategoryRepository userCategoryRepository;

    @Autowired
    private UserApplicationValidatorService<UserCategory> verificationService;

    @Autowired
    private ClaimResolver claimResolver;

    @Override
    @Transactional
    public void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto) {

        claimResolver.setToken(token);
        UserCategory userCategory = new UserCategory(categoryRequestDto);
        userCategory.setCreatedBy(claimResolver.getEmployee().orElse(null));
        Boolean exists = userCategoryRepository.existsByName(categoryRequestDto.getName());
        if(exists){
            throw new RuntimeException("Sorry! Category already exist");
        }

        DomainType domainType = DomainType.INVENTORY_REQ_CATEGORY;
        if(categoryRequestDto.getParentCategory()!=null) {
            domainType = DomainType.INVENTORY_REQ_SUB_CATEGORY;
            Optional<UserCategory> catOp = userCategoryRepository.findById(categoryRequestDto.getParentCategory().getId());
            if(catOp.isPresent()) {
                userCategory.setParentCategory(catOp.get());
            }
            userCategory.setAttributes(categoryRequestDto.getAttributes().stream()
                    .map(ca-> new UserCategoryAttribute(ca,userCategory))
                    .collect(Collectors.toList()));
            userCategory.setBrands(categoryRequestDto.getBrands().stream()
                    .map(cb->new UserCategoryBrand(cb,userCategory)).collect(Collectors.toList()));
        }

        userCategoryRepository.save(userCategory);

        verificationService.applyVerifyApprovalProcess(
                userCategory, domainType, UserCategoryStatus.COMPLETED.toString(),
                uri,domainType.toString(), List.of("-1"),
                null);
    }

    @Override
    public Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllCategoryByCreatedById(claimResolver.getUserId(),pageable);
    }

    @Override
    public Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllSubCategoryByCreatedById(claimResolver.getUserId(),pageable);
    }

    @Override
    public Page<?> getPendingVerifications(Jwt token, Optional<Integer> page, Optional<Integer> size, Boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(isCategory){
            return userCategoryRepository.findAllCategoryByNextVerifierId(
                    claimResolver.getUserId(),
                    pageable
            );
        }
        return userCategoryRepository.findAllSubCategoryByNextVerifierId(
                claimResolver.getUserId(),
                pageable
        );
    }
}
