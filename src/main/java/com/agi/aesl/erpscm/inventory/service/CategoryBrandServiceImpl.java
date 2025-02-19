package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryBrandDto;
import com.agi.aesl.erpscm.inventory.dto.request.RemoteCategoryRequestDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryBrandServiceImpl implements CategoryBrandService{

    @Autowired
    private CategoryBrandRepository categoryBrandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrgService orgService;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;

    @Override
    @Transactional
    public List<CategoryBrand> createBrands(Jwt token, List<CategoryBrandDto> brands) {
        List<CategoryBrand> cBrands = brands.stream().filter(b->{
            Optional<ItemCategory> catOp = categoryRepository.findById(b.getCategory().getId());
            if(catOp.isPresent()){
                ItemCategory category = catOp.get();
                this.sendBrandToCps(token,b.getName(),category.getCpsCategoryId());
                return true;
            }
            return false;
        }).map(filteredBrand->{
            return new CategoryBrand(null,filteredBrand.getName(),new ItemCategory(filteredBrand.getCategory().getId()),true);
        }).collect(Collectors.toList());
        return categoryBrandRepository.saveAll(cBrands);
    }

    @Transactional
    private void sendBrandToCps(Jwt token,String name, Long categoryId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId", orgOp.get().getCpsVendorRegistrationId().toString());
        }
        Map<String,Object> data = new HashMap<>();
        data.put("brandName",name);
        data.put("subCategory",new ReferenceObjectDto(categoryId));
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(data,headers);
        String url = cpsServerConfig.getPendingItemReqEndpoint()+"/pending-brands";
        System.out.println(url);
        ResponseEntity<?> response = networkService.post(url,payload,Void.class);
    }

}
