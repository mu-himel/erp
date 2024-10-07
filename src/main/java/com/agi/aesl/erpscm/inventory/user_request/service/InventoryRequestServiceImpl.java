package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.dto.request.UserItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.entity.ItemFunctionalUnit;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.inventory.user_request.entity.*;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryBrandRepository categoryBrandRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private UserItemHistoryRepository userItemHistoryRepository;

    @Autowired
    private UserApplicationValidatorService<UserItem> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private ItemService itemService;

    record MyCategory(Long id, String categoryName, Integer subCategoryCount, Integer productCount, String status){}
    record MySubCategory(Long id, String categoryName,String subCategoryName, Integer productCount, String status){}
    record MyProduct(Long id, String categoryName,String subCategoryName, String productName, String status){}

    @Override
    public Page<?> getMyProducts(Jwt token,
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
    public Page<?> getPendingVerifications(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                           Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingVerifications(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null), pageable);
    }

    @Override
    public Page<?> getPendingApprovals(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                       Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingApprovals(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),pageable);
    }

    @Override
    public Page<?> getClosed(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                             Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllClosed(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),pageable);
    }

    @Override
    public Page<?> getPendingApprovalItemsByStore(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                                  Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllPendingApprovalItemsByStore(claimResolver.getUserId(),
                categoryId.orElse(null),subCategoryId.orElse(null),pageable);
    }

    @Override
    @Transactional
    public void createProduct(Jwt token, String uri, UserItemRequestDto itemRequestDto) {
        claimResolver.setToken(token);
        Optional<Employee> empOp = claimResolver.getEmployee();
        if(empOp.isEmpty()){
            throw new RuntimeException("Sorry! Only Employee Can Create");
        }
        UserItem userItem = new UserItem(itemRequestDto);
        userItem.setActive(true);
        userItem.setWarehouse(new Warehouse(itemRequestDto.getWarehouse().getId()));
        userItem.setWarehouseStore(new WarehouseStore(itemRequestDto.getWarehouseStore().getId()));
        Optional<ItemCategory> catOp = categoryRepository.findById(itemRequestDto.getItemParentCategory().getId());
        if(catOp.isEmpty()) {
            throw new RuntimeException("Sorry! User Category not found");
        }
        Optional<ItemCategory> subCatOp = categoryRepository.findById(itemRequestDto.getItemCategory().getId());
        if(subCatOp.isEmpty()){
            throw new RuntimeException("Sorry! User Sub Category not found");
        }
        Optional<CategoryBrand> cbOp = categoryBrandRepository.findById(itemRequestDto.getBrand().getId());
        if(cbOp.isEmpty()) {
            throw new RuntimeException("Sorry! User Product not found");
        }
        userItem.setItemAttributeName(generateItemAttribute(userItem.getAttributes()));
        userItem.setCategory(catOp.get());
        userItem.setSubCategory(subCatOp.get());
        userItem.setBrand(cbOp.get());
        List<?> itemExistByAttr = this.getByAttributes(cbOp.get().getId(),userItem.getItemAttributeName());
        if(itemExistByAttr.size()>0){
            throw new RuntimeException("Sorry! Item Already exist with same attributes for this brand");
        }
        userItem.setCreatedBy(empOp.get());
        userItemRepository.save(userItem);

        AppliedVADto appliedVADto = verificationService.applyVerifyApprovalProcess(
                userItem, DomainType.INVENTORY_REQ_PRODUCT, UserCategoryStatus.COMPLETED.toString(),
                uri, DomainType.INVENTORY_REQ_PRODUCT.toString(), List.of("-1"),
                null);

        if(appliedVADto.getVerifiers().isEmpty() && appliedVADto.getPanels().isEmpty()){
              userItem.setItemStatus(UserCategoryStatus.COMPLETED);
//            try {
//                ObjectMapper mapper = new ObjectMapper();
//                String employee = mapper.writeValueAsString(userItem.getCreatedBy());
//                /**
//                 * Here may be need something more todo
//                 */
//                itemService.sendItemToCps(claimResolver,employee,userItem,userItem.getItemAttributes(),null);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    private String generateItemAttribute(List<UserItemAttribute> attributes){
        StringBuilder sb = new StringBuilder();

        attributes.stream().forEach(itemAttribute -> {
            sb.append(itemAttribute.getAttributeType().trim()
                    +" "+itemAttribute.getAttributeValue().trim()
                    +" "+itemAttribute.getAttributeUnit().trim());
            sb.append(" - ");
        });

        return (sb.isEmpty())? "" :  sb.toString().substring(0,sb.length()-3);
    }

    private List<?> getByAttributes(Long brandId, String attribute) {
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
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
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
//                ObjectMapper mapper = new ObjectMapper();
//                try {
//                    String employee = mapper.writeValueAsString(userItem.getCreatedBy());
//                    /**
//                     * Here may be need something more todo
//                     */
//                    itemService.sendItemToCps(claimResolver,employee,userItem,userItem.getItemAttributes(),null);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
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
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                String employee = mapper.writeValueAsString(userItem.getCreatedBy());
//                /**
//                 * Here may be need something more todo
//                 */
//                itemService.sendItemToCps(claimResolver,employee,userItem,userItem.getItemAttributes(),userItem.getWarehouseStore());
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
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
            throw new RuntimeException("User Item not found");
        }
        UserItem userItem = userItemOp.get();
        userItem.setReviewerId(null);
        if(userItem.getReviewPrevStatus()!=null) {
            userItem.setItemStatus(userItem.getReviewPrevStatus());
        }
        userItem.setReviewPrevStatus(null);
        userItem.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
                reviewDto.getDomainType(),
                reviewDto.getActionType(),
                userItem.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));

    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<UserItem> itemOp = userItemRepository.findById(domainId);
        itemOp.ifPresent((item)->{
            item.setItemStatus(UserCategoryStatus.REJECTED);
        });
    }

    @Override
    @Transactional
    public void approveByStore(Jwt token, Long id) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        itemOp.ifPresent((item)->{
            item.setIsApprovedByStore(true);
            ItemRequestDto itemRequestDto = new ItemRequestDto();
            itemRequestDto.setBrand(new ReferenceObjectDto(item.getBrand().getId()));
            itemRequestDto.setName(item.getName());
            itemRequestDto.setCode(item.getCode());
            itemRequestDto.setItemCategory(item.getSubCategory());
            itemRequestDto.setItemParentCategory(item.getCategory());
            itemRequestDto.setItemUnit(item.getItemUnit());
            if(item.getAttributes()!=null && !item.getAttributes().isEmpty()) {
                itemRequestDto.setAttributes(item.getAttributes().stream().map(_attr -> {
                    ItemAttribute attr = new ItemAttribute();
                    attr.setAttributeType(_attr.getAttributeType());
                    attr.setAttributeUnit(_attr.getAttributeUnit());
                    attr.setAttributeValue(_attr.getAttributeValue());
                    return attr;
                }).collect(Collectors.toList()));
            }
            if(item.getFunctionalUnits()!=null && !item.getFunctionalUnits().isEmpty()) {
                itemRequestDto.setFunctionalUnits(item.getFunctionalUnits().stream().map(_fu -> {
                    ItemFunctionalUnit ifu = new ItemFunctionalUnit();
                    ifu.setUnit(_fu.getUnit());
                    ifu.setValue(_fu.getValue());
                    return ifu;
                }).collect(Collectors.toList()));
            }
            itemService.createItem(token,itemRequestDto);
        });
    }

    @Override
    @Transactional
    public void rejectByStore(Long id) {
        Optional<UserItem> itemOp = userItemRepository.findById(id);
        itemOp.ifPresent((item)->{
            item.setIsApprovedByStore(false);
            item.setItemStatus(UserCategoryStatus.REJECTED);
        });
    }
}
