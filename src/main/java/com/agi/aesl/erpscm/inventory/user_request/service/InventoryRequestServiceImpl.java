package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.inventory.user_request.dto.CategoryRejectDto;
import com.agi.aesl.erpscm.inventory.user_request.entity.*;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserItemHistoryRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserItemRepository;
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
import org.springframework.data.domain.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventoryRequestServiceImpl implements InventoryRequestService{

    private static final Integer PAGE_SIZE = 20;

    private final ClaimResolver claimResolver;


    private final CategoryRepository categoryRepository;


    private final CategoryBrandRepository categoryBrandRepository;


    private final UserItemRepository userItemRepository;


    private final UserItemHistoryRepository userItemHistoryRepository;


    private final UserApplicationValidatorService<UserItem> verificationService;


    private final CommentService commentService;


    private final WarehouseService warehouseService;


    private final ItemService itemService;

    record MyCategory(Long id, String categoryName, Integer subCategoryCount, Integer productCount, String status){}
    record MySubCategory(Long id, String categoryName,String subCategoryName, Integer productCount, String status){}
    record MyProduct(Long id, String categoryName,String subCategoryName, String productName, String status){}

    @Override
    public Page<UserItemRepository.UserItem> getMyProducts(Jwt token,
                                                           Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                           Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllByCreatedById(
                claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),
                pageable
        );
    }

    @Override
    public Page<UserItemRepository.UserItem> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                                     Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingVerifications(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null), pageable);
    }

    @Override
    public Page<UserItemRepository.UserItem> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                                 Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingApprovals(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),pageable);
    }

    @Override
    public Page<UserItemRepository.UserItem> getClosed(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                       Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllClosed(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),pageable);
    }

    @Override
    public Page<UserItemRepository.PendingApprovalUserItem> getPendingApprovalItemsByStore(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                                                           Optional<Long> warehouseId, Optional<Long> warehouseStoreId,
                                                                                           Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingApprovalItemsByStore(
                categoryId.orElse(null),subCategoryId.orElse(null),
                warehouseId.orElse(null),warehouseStoreId.orElse(null),
                pageable);
    }

    @Override
    @Transactional
    public void createProduct(Jwt token, String uri, UserItemRequestDto itemRequestDto) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new AesException("Sorry! Only Employee Can Create");
        }
        UserItem userItem = new UserItem(itemRequestDto);
        userItem.setActive(true);
        userItem.setWarehouse(new Warehouse(itemRequestDto.getWarehouse().getId()));
        userItem.setWarehouseStore(new WarehouseStore(itemRequestDto.getWarehouseStore().getId()));
        Optional<ItemCategory> catOp = categoryRepository.findById(itemRequestDto.getItemParentCategory().getId());
        if(catOp.isEmpty()) {
            throw new AesException("Sorry! User Category not found");
        }
        Optional<ItemCategory> subCatOp = categoryRepository.findById(itemRequestDto.getItemCategory().getId());
        if(subCatOp.isEmpty()){
            throw new AesException("Sorry! User Sub Category not found");
        }
        Optional<CategoryBrand> cbOp = categoryBrandRepository.findById(itemRequestDto.getBrand().getId());
        if(cbOp.isEmpty()) {
            throw new AesException("Sorry! User Product not found");
        }
        userItem.setItemAttributeName(generateItemAttribute(userItem.getAttributes()));
        userItem.setCategory(catOp.get());
        userItem.setSubCategory(subCatOp.get());
        userItem.setBrand(cbOp.get());
        List<?> itemExistByAttr = this.getByAttributes(cbOp.get().getId(),userItem.getItemAttributeName());
        if(!itemExistByAttr.isEmpty()){
            throw new AesException("Sorry! Item Already exist with same attributes for this brand");
        }
        userItem.setCreatedBy(empOp.get());
        userItemRepository.save(userItem);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(
                userItem, DomainType.INVENTORY_REQ_PRODUCT, UserCategoryStatus.PENDING.toString(),
                uri, DomainType.INVENTORY_REQ_PRODUCT.toString(), List.of("-1"),
                null);

        if(appliedVADto.getVerifiers().isEmpty() && appliedVADto.getPanels().isEmpty()){
              userItem.setItemStatus(UserCategoryStatus.PENDING);
        }
    }

    private String generateItemAttribute(List<UserItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.forEach(itemAttribute -> {
            String s = "";
            s=s.concat(itemAttribute.getAttributeType().trim()
                    +" "+itemAttribute.getAttributeValue().trim()
                    +" "+itemAttribute.getAttributeUnit().trim());
            s=s.concat(" - ");
            sb.append(s);
        });

        return (sb.isEmpty())? "" :  sb.substring(0,sb.length()-3);
    }

    private List<UserItemRepository.UserItemByAttribute> getByAttributes(Long brandId, String attribute) {
        return userItemRepository.findByAttributes(brandId,attribute);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        Map<String,Object> detail = new HashMap<>();
        if(itemOp.isPresent()){
            UserItem item = itemOp.get();
            detail.put("detail",item);
            detail.put("warehouse",warehouseService.getWarehouse(item.getCreatedBy().getWarehouseId()));
            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            DomainType domainType = DomainType.INVENTORY_REQ_PRODUCT;
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(domainType, item.getId());
            vrs.forEach(verifier->{
                if(Boolean.FALSE.equals(verifier.getIsApproval())){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(domainType, item.getId());
            detail.put("verifiers",verifiers);
            detail.put("approvers",approvers);
            detail.put("comments",comments);
        }
        return detail;
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        if(itemOp.isPresent()){
            UserItem item = itemOp.get();
            item.setNextVerifierId(nextVerifier.getVerifier().getId());
            saveHistory(item,verification.getVerifier(),UserCategoryStatus.VERIFIED);

        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        if(itemOp.isPresent()){
            UserItem item = itemOp.get();
            item.setNextApproverId(nextApprover.getVerifier().getId());
            saveHistory(item,verification.getVerifier(),UserCategoryStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        if(itemOp.isPresent()){
            UserItem userItem = itemOp.get();
            if(firstApprover.isPresent()){
                userItem.setNextApproverId(firstApprover.get().getVerifier().getId());
                userItem.setItemStatus(UserCategoryStatus.PENDING_APPROVAL);
            }else {
                userItem.setItemStatus(UserCategoryStatus.VERIFIED);
            }
            saveHistory(userItem,new Employee(userItem.getNextVerifierId()),UserCategoryStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        if(itemOp.isPresent()){
            UserItem userItem = itemOp.get();
            userItem.setItemStatus(UserCategoryStatus.APPROVED);
            saveHistory(userItem,new Employee(userItem.getNextApproverId()),UserCategoryStatus.APPROVED);
        }
    }

    private void saveHistory(UserItem userItem,Employee employee,UserCategoryStatus status){
        UserItemHistory userItemHistory = new UserItemHistory();
        userItemHistory.setEmployee(employee);
        userItemHistory.setUserItem(userItem);
        userItemHistory.setItemStatus(status);
        userItemHistoryRepository.save(userItemHistory);
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<UserItem> itemOp = userItemRepository.findById(domainId);
        if(itemOp.isPresent()){
            UserItem item = itemOp.get();
            if(!item.getItemStatus().equals(UserCategoryStatus.REVIEW)){
                item.setReviewPrevStatus(item.getItemStatus());
                item.setItemStatus(UserCategoryStatus.REVIEW);
            }
            item.setReviewerId(reviewer.getId());
            item.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void review(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        Optional<UserItem> userItemOp = userItemRepository.findById(id);
        if(userItemOp.isEmpty()){
            throw new AesException("User Item not found");
        }
        UserItem userItem = userItemOp.get();
        userItem.setReviewerId(null);
        if(userItem.getReviewPrevStatus()!=null) {
            userItem.setItemStatus(userItem.getReviewPrevStatus());
        }
        userItem.setReviewPrevStatus(null);
        userItem.setReviewDate(LocalDateTime.now());

        claimResolver.getEmployee().ifPresent( emp->
        commentService.addComment(commentService.prepareComment(
                    emp,
                    reviewDto.getDomainType(),
                    reviewDto.getActionType(),
                    userItem.getId(),
                    reviewDto.getMessage(),
                    reviewDto.getAttachments()
            ))
        );
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<UserItem> itemOp = userItemRepository.findById(domainId);
        itemOp.ifPresent(item->
            item.setItemStatus(UserCategoryStatus.REJECTED)
        );
    }

    @Override
    @Transactional
    public void approveByStore(Jwt token, Long id) {

        Optional<UserItem> itemOp = userItemRepository.findById(id);
        itemOp.ifPresent(item->{
            item.setItemStatus(UserCategoryStatus.PENDING_CPS);
            item.setIsApprovedByStore(true);
            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setBrand(new ReferenceObjectDto(item.getBrand().getId()));
            itemRequestDto.setName(item.getName());
            itemRequestDto.setUserItemId(item.getId());
            itemRequestDto.setItemCategory(item.getSubCategory());
            itemRequestDto.setItemParentCategory(item.getCategory());
            itemRequestDto.setItemUnit(item.getItemUnit());
            itemRequestDto.setWarehouse(new ReferenceObjectDto(item.getWarehouse().getId()));
            itemRequestDto.setWarehouseStore(new ReferenceObjectDto(item.getWarehouseStore().getId()));
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonStr = null;
            try {
                jsonStr = objectMapper.writeValueAsString(item.getCreatedBy());
            } catch (JsonProcessingException e) {
                throw new AesException(e.getMessage());
            }
            itemRequestDto.setEmployee(jsonStr);
            if(item.getAttributes()!=null && !item.getAttributes().isEmpty()) {
                itemRequestDto.setAttributes(item.getAttributes().stream().map(attr -> {
                    ItemAttribute iAttr = new ItemAttribute();
                    iAttr.setAttributeType(attr.getAttributeType());
                    iAttr.setAttributeUnit(attr.getAttributeUnit());
                    iAttr.setAttributeValue(attr.getAttributeValue());
                    return iAttr;
                }).toList());
            }
            if(item.getFunctionalUnits()!=null && !item.getFunctionalUnits().isEmpty()) {
                itemRequestDto.setFunctionalUnits(item.getFunctionalUnits().stream().map(fu -> {
                    ItemFunctionalUnit ifu = new ItemFunctionalUnit();
                    ifu.setUnit(fu.getUnit());
                    ifu.setValue(fu.getValue());
                    return ifu;
                }).toList());
            }
            itemService.createItem(token,itemRequestDto);
        });
    }

    @Override
    @Transactional
    public void rejectByStore(Long id, CategoryRejectDto rejectDto) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        itemOp.ifPresent(item->{
            item.setIsApprovedByStore(false);
            item.setRejectNoteFromStore(rejectDto.getNote());
            item.setItemStatus(UserCategoryStatus.REJECTED);
        });
    }
}
