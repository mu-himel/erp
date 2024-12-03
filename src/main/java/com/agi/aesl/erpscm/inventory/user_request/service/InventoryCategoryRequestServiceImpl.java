package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.account_finance.enums.AccountType;
import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.entity.IndentVerificationApprovalHistory;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.indent.enums.RfqStatus;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteCategoryRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.service.CategoryService;
import com.agi.aesl.erpscm.inventory.service.ItemService;
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
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.entity.QualityControl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryCategoryRequestServiceImpl implements InventoryCategoryRequestService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private UserCategoryRepository userCategoryRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private UserApplicationValidatorService<UserCategory> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private OrgService orgService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserCategoryHistoryRepository userCategoryHistoryRepository;

    @Override
    @Transactional
    public void createCategory(Jwt token, String uri, CategoryRequestDto categoryRequestDto) {

        claimResolver.setToken(token);
        UserCategory userCategory = new UserCategory(categoryRequestDto);
        userCategory.setCreatedBy(claimResolver.getEmployee().orElse(null));
        userCategory.setStore(new WarehouseStore(categoryRequestDto.getWarehouseStore().getId()));
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
            } else {
                Optional<ItemCategory> _catOp = categoryService.getItemCategoryById(categoryRequestDto.getParentCategory().getId());
                _catOp.ifPresent(userCategory::setActiveParentCategory);
            }

            userCategory.setVat(categoryRequestDto.getVat());

            userCategory.setAttributes(categoryRequestDto.getAttributes().stream()
                    .map(ca-> new UserCategoryAttribute(ca,userCategory))
                    .collect(Collectors.toList()));
            userCategory.setBrands(categoryRequestDto.getBrands().stream()
                    .map(cb->new UserCategoryBrand(cb,userCategory)).collect(Collectors.toList()));
        }

        userCategoryRepository.save(userCategory);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(
                userCategory, domainType, UserCategoryStatus.COMPLETED.toString(),
                uri, domainType.toString(), List.of("-1"),
                null);

        if(appliedVADto.getVerifiers().isEmpty() && appliedVADto.getPanels().isEmpty()){
              userCategory.setCategoryStatus(UserCategoryStatus.COMPLETED);
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                String employee = mapper.writeValueAsString(userCategory.getCreatedBy());
//                categoryService.sendToCps(token, userCategory, employee);
//            }catch (Exception ex){
//                throw new RuntimeException(ex.getMessage());
//            }
        }
    }

    @Override
    public List<?> getCategories(Jwt token, Optional<String> name, Optional<String> code,Optional<Long>storeId) {
        claimResolver.setToken(token);
        return userCategoryRepository.getAllCategories(name.orElse(null),code.orElse(null),
                storeId.orElse(null));
    }

    @Override
    public List<?> getSubCategories(Jwt token, Long categoryId, Optional<String> name, Optional<String> code) {
        claimResolver.setToken(token);
        return userCategoryRepository.getAllSubCategories(categoryId,
                name.orElse(null),code.orElse(null));
    }

    @Override
    public Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllCategoryByCreatedById(claimResolver.getUserId(),pageable);
    }

    @Override
    public Page<?> getMySubCategories(Jwt token,Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userCategoryRepository.findAllSubCategoryByCreatedById(claimResolver.getUserId(),categoryId.orElse(null),pageable);
    }

    @Override
    public Page<?> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, Boolean isCategory) {
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
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public Page<?> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(isCategory){
            return userCategoryRepository.findAllCategoryByNextApproverId(
                    claimResolver.getUserId(),
                    pageable
            );
        }
        return userCategoryRepository.findAllSubCategoryByNextApproverId(
                claimResolver.getUserId(),
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public Page<?> getClosed(Jwt token,Optional<Long> categoryId, Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        if(isCategory){
            return userCategoryRepository.findAllClosed(claimResolver.getUserId(),pageable);
        }
        return userCategoryRepository.findAllClosedSubCategory(
                claimResolver.getUserId(),
                categoryId.orElse(null),
                pageable
        );
    }

    @Override
    public Page<?> getPendingApprovalCategoriesFromStore(Jwt token, Optional<Long> categoryId,
        Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
        Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);

        return userCategoryRepository.findAllPendingApprovalByStore(null,
                warehouseId.orElse(null),warehouseStoreId.orElse(null),pageable);

    }

    @Override
    public Page<?> getPendingApprovalSubCategoriesFromStore(Jwt token, Optional<Long> categoryId,
                                Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                Optional<Integer> page, Optional<Integer> size, boolean isCategory) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);

        return userCategoryRepository
                .findAllPendingApprovalByStore(categoryId.orElse(null),
                        warehouseId.orElse(null),warehouseStoreId.orElse(null),
                        pageable);

    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Optional<UserCategory> catOp = userCategoryRepository.findById(id);
        if(catOp.isEmpty()){
            throw new RuntimeException("Sorry! Category not found");
        }
        Map<String,Object> detail = new HashMap<>();
        UserCategory category = catOp.get();
        detail.put("detail",category);
        detail.put("warehouse",warehouseService.getWarehouse(category.getCreatedBy().getWarehouseId()));
        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        DomainType domainType = (category.getParentCategory()!=null)? DomainType.INVENTORY_REQ_SUB_CATEGORY:
                DomainType.INVENTORY_REQ_CATEGORY;
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(domainType, category.getId());
        vrs.stream().forEach(verifier->{
            if(verifier.getIsApproval()==false){
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
//                ObjectMapper mapper = new ObjectMapper();
//                try {
//                    String employee = mapper.writeValueAsString(category.getCreatedBy());
//                    categoryService.sendToCps(claimResolver.getToken(),category,employee);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//                IndentVerificationApprovalHistory userCatHistory = new IndentVerificationApprovalHistory();
//                userCatHistory.setIndent(indent);
//                userCatHistory.setEmployee(new Employee(indent.getNextVerifierId()));
//                userCatHistory.setIndentStatus(IndentVerificationStatus.VERIFIED);
//                indentVARepository.save(userCatHistory);
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
//            ObjectMapper mapper = new ObjectMapper();

//            try {
//                String employee = mapper.writeValueAsString(category.getCreatedBy());
//                categoryService.sendToCps(claimResolver.getToken(),category,employee);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }


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
            throw new RuntimeException("QC not found");
        }
        UserCategory userCategory = userCatOp.get();
        userCategory.setReviewerId(null);
        if(userCategory.getReviewPrevStatus()!=null) {
            userCategory.setCategoryStatus(userCategory.getReviewPrevStatus());
        }
        userCategory.setReviewPrevStatus(null);
        userCategory.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
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
        catOp.ifPresent((cat)->{
            cat.setCategoryStatus(UserCategoryStatus.REJECTED);
        });
    }

    @Override
    @Transactional
    public void approveByStore(Jwt token, Long domainId, CategoryApproveDto categoryApproveDto) {
        if(categoryApproveDto.getPrefix()==null){
            throw new RuntimeException("Sorry! prefix required");
        }
        Optional<UserCategory> catOp = userCategoryRepository.findById(domainId);
        catOp.ifPresent((cat)->{
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
            }).collect(Collectors.toList()));
            categoryRequestDto.setBrands(cat.getBrands().stream().map(UserCategoryBrand::getName).collect(Collectors.toList()));
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
                throw new RuntimeException(e);
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
        catOp.ifPresent((cat)->{
            cat.setIsApprovedByStore(false);
            cat.setRejectNoteFromStore(noteDto.getNote());
            cat.setCategoryStatus(UserCategoryStatus.REJECTED);
        });
    }

    //    private void sentToCps(Jwt token, UserCategory category){
//        RemoteCategoryRequestDto remoteCategoryRequestDto = new RemoteCategoryRequestDto();
//        remoteCategoryRequestDto.setName(category.getName());
//        remoteCategoryRequestDto.setCode(category.getCode());
//
//        if(category.getParentCategory()!=null) {
//            Optional<UserCategory> parentCategoryOp = userCategoryRepository.findById(category.getParentCategory().getId());
//
//            if (parentCategoryOp.isPresent()) {
//                remoteCategoryRequestDto.setParentCategory(new ReferenceObjectDto(parentCategoryOp.get().getCpsCategoryId()));
//            }
//
//            remoteCategoryRequestDto.setAttributes(category.getAttributes().stream().map(attr->{
//                CategoryAttribute ca = new CategoryAttribute();
//                ca.setAttributeType(attr.getAttributeType());
//                ca.setAttributeUnit(attr.getAttributeUnit());
//                ca.setAttributeValue(attr.getAttributeValue());
//                return ca;
//            }).collect(Collectors.toList()));
//            remoteCategoryRequestDto.setBrands(category.getBrands().stream().map(b->b.getName()).collect(Collectors.toList()));
//            remoteCategoryRequestDto.setVat(category.getVat());
//        }
//        remoteCategoryRequestDto.setScmCategoryId(category.getId());
//        remoteCategoryRequestDto.setCreatedBy(category.getCreatedBy().getId());
//        remoteCategoryRequestDto.setCategoryStatus("PENDING");
//        remoteCategoryRequestDto.setIsUserGenerated(true);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
//        if(orgOp.isPresent()){
//            headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
//        }
//        HttpEntity<RemoteCategoryRequestDto> payload = new HttpEntity<>(remoteCategoryRequestDto,headers);
//        String url = cpsServerConfig.getItemCategoriesEndpoint();
//        ResponseEntity<?> response = networkService.post(url,payload,Void.class);
//        HttpHeaders httpHeaders = response.getHeaders();
//        List<String> headerId = httpHeaders.get("id");
//        if(headerId.size()>0){
//            category.setCpsCategoryId(Long.parseLong(headerId.get(0)));
//        }
//    }
}
