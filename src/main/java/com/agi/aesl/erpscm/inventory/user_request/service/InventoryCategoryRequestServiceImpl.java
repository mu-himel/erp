package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryApproveDto;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryAttribute;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryBrand;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryHistory;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryHistoryRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryCategoryRequestServiceImpl implements InventoryCategoryRequestService{

    private static final Integer PAGE_SIZE = 20;

    private final UserCategoryRepository userCategoryRepository;


    private final ClaimResolver claimResolver;


    private final UserApplicationValidatorService<UserCategory> verificationService;


    private final CommentService commentService;


    private final WarehouseService warehouseService;


    private final OrgService orgService;


    private final NetworkService networkService;


    private final CpsServerConfig cpsServerConfig;


    private final CategoryService categoryService;


    private final IntegrationReaderService integrationReaderService;


    private final UserCategoryHistoryRepository userCategoryHistoryRepository;

    private Employee getEmp(){
        return claimResolver.getEmployee().orElse(null);
    }

    private Long getEmpWarehouseId(){
        Employee emp = claimResolver.getEmployee().orElse(null);
        return (emp!=null)? emp.getWarehouseId() : null;
    }

    @Override
    @Transactional
    public void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto) {

        claimResolver.setToken(token);
        UserCategory userCategory = new UserCategory(categoryRequestDto);
        userCategory.setCreatedBy(claimResolver.getEmployee().orElse(null));
        userCategory.setStore(new WarehouseStore(categoryRequestDto.getWarehouseStore().getId()));
        Boolean exists = userCategoryRepository.existsByName(categoryRequestDto.getName());
        if(Boolean.TRUE.equals(exists)){
            throw new AesException("Sorry! Category already exist");
        }

        DomainType domainType = DomainType.INVENTORY_REQ_CATEGORY;
        if(categoryRequestDto.getParentCategory()!=null) {
            domainType = DomainType.INVENTORY_REQ_SUB_CATEGORY;
            Optional<ItemCategory> catOp = categoryService.getItemCategoryById(categoryRequestDto.getParentCategory().getId());
            catOp.ifPresent(userCategory::setActiveParentCategory);

            userCategory.setVat(categoryRequestDto.getVat());

            userCategory.setAttributes(categoryRequestDto.getAttributes().stream()
                    .map(ca-> new UserCategoryAttribute(ca,userCategory))
                    .toList());
            userCategory.setBrands(categoryRequestDto.getBrands().stream()
                    .map(cb->new UserCategoryBrand(cb,userCategory)).toList());
        }

        userCategoryRepository.save(userCategory);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(
                userCategory, domainType, UserCategoryStatus.PENDING.toString(),
                uri, domainType.toString(), List.of("-1"),
                null);

        if(appliedVADto.getVerifiers().isEmpty() && appliedVADto.getPanels().isEmpty()){
              userCategory.setCategoryStatus(UserCategoryStatus.PENDING);
        }
    }

    @Override
    public List<UserCategoryRepository.UserCategoryInfo> getCategories(Jwt token, Optional<String> name, Optional<String> code,
                                                                       Optional<Long>warehouseIdOp,
                                                                       Optional<Long>storeId) {
        claimResolver.setToken(token);
        Long warehouseId = null;
        if(warehouseIdOp.isPresent()){
            warehouseId = warehouseIdOp.get();
        }
        if(claimResolver.getEmployee().isPresent()){
            warehouseId = getEmpWarehouseId();
        }
        return userCategoryRepository.getAllCategories(name.orElse(null),code.orElse(null),
               warehouseId, storeId.orElse(null));
    }

    @Override
    public List<UserCategoryRepository.UserCategoryInfo> getSubCategories(Jwt token, Long categoryId, Optional<String> name, Optional<String> code) {
        claimResolver.setToken(token);
        Long warehouseId = null;
        if(claimResolver.getEmployee().isPresent()){
            warehouseId = getEmpWarehouseId();
        }
        return userCategoryRepository.getAllSubCategories(categoryId,
                name.orElse(null),code.orElse(null), warehouseId);
    }

    @Override
    public Page<UserCategoryRepository.UserCategory> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllCategoryByCreatedById(claimResolver.getUserId(),pageable);
    }

    @Override
    public Page<UserCategoryRepository.UserSubCategory> getMySubCategories(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllSubCategoryByCreatedById(claimResolver.getUserId(),categoryId.orElse(null),pageable);
    }

    @Override
    public <T> T getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, Boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(Boolean.TRUE.equals(isCategory)){
            return (T) userCategoryRepository.findAllCategoryByNextVerifierId(
                    claimResolver.getUserId(),
                    pageable
            );
        }
        return (T) userCategoryRepository.findAllSubCategoryByNextVerifierId(
                claimResolver.getUserId(),
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public <T> T getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(isCategory){
            return (T) userCategoryRepository.findAllCategoryByNextApproverId(
                    claimResolver.getUserId(),
                    pageable
            );
        }
        return (T) userCategoryRepository.findAllSubCategoryByNextApproverId(
                claimResolver.getUserId(),
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public <T> T getClosed(Jwt token,Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(isCategory){
            return (T) userCategoryRepository.findAllClosed(claimResolver.getUserId(),pageable);
        }
        return  (T) userCategoryRepository.findAllClosedSubCategory(
                claimResolver.getUserId(),
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public Page<UserCategoryRepository.PendingApprovalStore> getPendingApprovalCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                                                                                   Optional<String> name,
                                                                                                   Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                                                                                   Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        String uri="inventory-control/categories";
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = new ArrayList<>();
        if(warehouseId.isPresent()){
            warehouseIds.add(warehouseId.get());
        }else {
            warehouseIds = dataFilter.getFilterConfig(DataFilter.FILTER_BY_WAREHOUSE);
        }
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);

        return userCategoryRepository.findAllPendingApprovalByStore(null,
                name.orElse(null),
                warehouseIds,warehouseStoreId.orElse(null),pageable);

    }

    @Override
    public Page<UserCategoryRepository.PendingApprovalStore> getPendingApprovalSubCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                                                                                      Optional<String> name,
                                                                                                      Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                                                                                      Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);

        return userCategoryRepository
                .findAllPendingApprovalSubCatByStore(categoryId.orElse(null),
                        name.orElse(null),
                        warehouseId.orElse(null),warehouseStoreId.orElse(null),
                        pageable);

    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isEmpty()){
            throw new AesException("Sorry! Category not found");
        }
        Map<String,Object> detail = new HashMap<>();
        UserCategory category = catOp.get();
        if(category.getCategoryStatus().equals(UserCategoryStatus.COMPLETED)){
            Optional<ItemCategory> userCatOp = categoryService.getCategoryByUserCategory(category.getId());
            userCatOp.ifPresent(cat->category.setCode(cat.getCode()));
        }
        detail.put("detail",category);
        detail.put("warehouse",warehouseService.getWarehouse(category.getCreatedBy().getWarehouseId()));
        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        DomainType domainType = (category.getParentCategory()!=null)? DomainType.INVENTORY_REQ_SUB_CATEGORY:
                DomainType.INVENTORY_REQ_CATEGORY;
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(domainType, category.getId());
        vrs.forEach(verifier->{
            if(Boolean.FALSE.equals(verifier.getIsApproval())){
                verifiers.add(verifier);
            }else{
                approvers.add(verifier);
            }
        });

        List<?> comments = commentService.getCommentsByDomain(domainType, category.getId());
        detail.put("verifiers",verifiers);
        detail.put("approvers",approvers);
        detail.put("comments",comments);
        return detail;
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isPresent()){
            UserCategory category = catOp.get();
            category.setNextVerifierId(nextVerifier.getVerifier().getId());
            saveHistory(category,verification.getVerifier(),UserCategoryStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isPresent()){
            UserCategory category = catOp.get();
            category.setNextApproverId(nextApprover.getVerifier().getId());
            saveHistory(category,verification.getVerifier(),UserCategoryStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isPresent()) {
            UserCategory category = catOp.get();
            if (firstApprover.isPresent()) {


                category.setNextApproverId(firstApprover.get().getVerifier().getId());
                category.setCategoryStatus(UserCategoryStatus.PENDING_APPROVAL);

            } else {

                category.setCategoryStatus(UserCategoryStatus.VERIFIED);
            }
            saveHistory(category, new Employee(category.getNextVerifierId()),UserCategoryStatus.VERIFIED);
        }
    }

    @Transactional
    private void saveHistory(UserCategory userCategory,Employee employee,UserCategoryStatus status){
        UserCategoryHistory userCatHistory = new UserCategoryHistory();
        userCatHistory.setUserCategory(userCategory);
        userCatHistory.setEmployee(employee);
        userCatHistory.setCategoryStatus(status);
        userCategoryHistoryRepository.save(userCatHistory);
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isPresent()){
            UserCategory category = catOp.get();
            category.setStatus(String.valueOf(IndentVerificationStatus.APPROVED));

            saveHistory(category,new Employee(category.getNextApproverId()),UserCategoryStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<UserCategory> catOp  = userCategoryRepository.findById(domainId);
        if(catOp.isPresent()){
            UserCategory category = catOp.get();
            if(!category.getCategoryStatus().equals(UserCategoryStatus.REVIEW)){
                category.setReviewPrevStatus(category.getCategoryStatus());
                category.setCategoryStatus(UserCategoryStatus.REVIEW);
            }
            category.setReviewerId(reviewer.getId());
            category.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void review(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<UserCategory> userCatOp = userCategoryRepository.findById(id);
        if(userCatOp.isEmpty()){
            throw new AesException("QC not found");
        }
        UserCategory userCategory = userCatOp.get();
        userCategory.setReviewerId(null);
        if(userCategory.getReviewPrevStatus()!=null) {
            userCategory.setCategoryStatus(userCategory.getReviewPrevStatus());
        }
        userCategory.setReviewPrevStatus(null);
        userCategory.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                getEmp(),
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                userCategory.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(domainId);
        catOp.ifPresent(cat->
            cat.setCategoryStatus(UserCategoryStatus.REJECTED)
        );
    }

    @Override
    @Transactional
    public void approveByStore(Jwt token, Long domainId, CategoryApproveDto categoryApproveDto) {
        if(categoryApproveDto.getPrefix()==null){
            throw new AesException("Sorry! prefix required");
        }
        Optional<UserCategory> catOp = userCategoryRepository.findById(domainId);
        catOp.ifPresent(cat->{
            cat.setCategoryStatus(UserCategoryStatus.PENDING_CPS);
            cat.setIsApprovedByStore(true);
            CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
            categoryRequestDto.setUserCategoryId(cat.getId());
            categoryRequestDto.setName(cat.getName());
            categoryRequestDto.setPrefix(categoryApproveDto.getPrefix());
            categoryRequestDto.setAttributes(cat.getAttributes().stream().map(ca->{
                CategoryAttribute cattr = new CategoryAttribute();
                cattr.setAttributeType(ca.getAttributeType());
                cattr.setAttributeValue(ca.getAttributeValue());
                cattr.setAttributeUnit(ca.getAttributeUnit());
                return cattr;
            }).toList());
            categoryRequestDto.setBrands(cat.getBrands().stream().map(UserCategoryBrand::getName).toList());
            if(cat.getParentCategory()!=null){
                ItemCategory category = new ItemCategory(cat.getParentCategory().getId());
                categoryRequestDto.setParentCategory(category);
                categoryRequestDto.setCurrentYearBudget(new BigDecimal(0));
            }else if(cat.getActiveParentCategory()!=null){
                ItemCategory category = new ItemCategory(cat.getActiveParentCategory().getId());
                categoryRequestDto.setParentCategory(category);
                categoryRequestDto.setCurrentYearBudget(new BigDecimal(0));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = null;
            try {
                jsonStr = objectMapper.writeValueAsString(cat.getCreatedBy());
            } catch (JsonProcessingException e) {
                throw new AesException(e.getMessage());
            }
            categoryRequestDto.setEmployee(jsonStr);
            categoryRequestDto.setWarehouse(new ReferenceObjectDto(cat.getStore().getWarehouse().getId()));
            categoryRequestDto.setWarehouseStore(new ReferenceObjectDto(cat.getStore().getId()));
            categoryRequestDto.setIsForCps(true);
            categoryService.addCategory(token, categoryRequestDto);


        });
    }

    @Override
    @Transactional
    public void rejectByStore(Long domainId, CategoryRejectDto noteDto) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(domainId);
        catOp.ifPresent(cat->{
            cat.setIsApprovedByStore(false);
            cat.setRejectNoteFromStore(noteDto.getNote());
            cat.setCategoryStatus(UserCategoryStatus.REJECTED);
        });
    }
}
