package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.exception.AesException;
import com.agi.aesl.erpscm.inventory.dto.request.ItemRequestDto;
import com.agi.aesl.erpscm.inventory.entity.ItemAttribute;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategory;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserCategoryBrand;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserItem;
import com.agi.aesl.erpscm.inventory.user_request.entity.UserItemAttribute;
import com.agi.aesl.erpscm.inventory.user_request.enums.UserCategoryStatus;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserCategoryRepository;
import com.agi.aesl.erpscm.inventory.user_request.repository.UserItemRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private UserCategoryRepository userCategoryRepository;

    @Autowired
    private UserCategoryBrandRepository userCategoryBrandRepository;

    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private UserApplicationValidatorService<UserItem> verificationService;

    record MyCategory(Long id, String categoryName, Integer subCategoryCount, Integer productCount, String status){}
    record MySubCategory(Long id, String categoryName,String subCategoryName, Integer productCount, String status){}
    record MyProduct(Long id, String categoryName,String subCategoryName, String productName, String status){}
    @Override
    public Page<?> getMyCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        List<MyCategory> lists = new ArrayList<>();
        lists.add(new MyCategory(1L,"R#4920-Civil",5,10,"PENDING"));
        lists.add(new MyCategory(2L,"R#4920-Civil",5,10,"PENDING"));
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(1));
        return new PageImpl<>(lists,pageable,lists.size());
    }

    @Override
    public Page<?> getMySubCategories(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        List<MySubCategory> lists = new ArrayList<>();
        lists.add(new MySubCategory(1L,"R#4920-Civil","01AbHF- Mouse",10,"PENDING"));
        lists.add(new MySubCategory(2L,"R#4920-Civil","01AbHF- Keyboard",8,"PENDING"));
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(1));
        return new PageImpl<>(lists,pageable,lists.size());
    }

    @Override
    public Page<?> getMyProducts(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE),sort);
        return userItemRepository.findAllByCreatedById(
                claimResolver.getUserId(),
                pageable
        );
    }

    @Override
    @Transactional
    public void createProduct(Jwt token, String uri, ItemRequestDto itemRequestDto) {
        UserItem userItem = new UserItem(itemRequestDto);
        userItem.setActive(true);

        Optional<UserCategory> catOp = userCategoryRepository.findById(itemRequestDto.getItemParentCategory().getId());
        if(catOp.isEmpty()) {
            throw new RuntimeException("Sorry! User Category not found");
        }
        Optional<UserCategory> subCatOp = userCategoryRepository.findById(itemRequestDto.getItemCategory().getId());
        if(subCatOp.isEmpty()){
            throw new RuntimeException("Sorry! User Sub Category not found");
        }
        Optional<UserCategoryBrand> cbOp = userCategoryBrandRepository.findById(itemRequestDto.getBrand().getId());
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
        userItemRepository.save(userItem);

        verificationService.applyVerifyApprovalProcess(
                userItem, DomainType.INVENTORY_REQ_PRODUCT, UserCategoryStatus.COMPLETED.toString(),
                uri,DomainType.INVENTORY_REQ_PRODUCT.toString(), List.of("-1"),
                null);
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
}
