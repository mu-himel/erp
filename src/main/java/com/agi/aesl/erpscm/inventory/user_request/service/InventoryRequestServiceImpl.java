package com.agi.aesl.erpscm.inventory.user_request.service;

import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryRequestServiceImpl implements InventoryRequestService{

    @Autowired
    private ClaimResolver claimResolver;
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
        List<MyProduct> lists = new ArrayList<>();
        lists.add(new MyProduct(1L,"R#4920-Civil","01AbHF- Mouse","A4 tech mouse","PENDING"));
        lists.add(new MyProduct(2L,"R#4920-Civil","01AbHF- Keyboard","A4 tech keyboard","PENDING"));
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(1));
        return new PageImpl<>(lists,pageable,lists.size());
    }
}
