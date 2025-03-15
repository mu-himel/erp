package com.agi.aesl.erpscm.inventory.service;

import com.agi.aesl.erpscm.common.ReferenceObjectDto;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.inventory.dto.request.CategoryBrandDto;
import com.agi.aesl.erpscm.inventory.entity.CategoryBrand;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.inventory.repository.CategoryBrandRepository;
import com.agi.aesl.erpscm.inventory.repository.CategoryRepository;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryBrandServiceImpl implements CategoryBrandService{


    private final CategoryBrandRepository categoryBrandRepository;


    private final CategoryRepository categoryRepository;


    private final OrgService orgService;


    private final NetworkService networkService;


    private final CpsServerConfig cpsServerConfig;

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
        }).map(filteredBrand->
             new CategoryBrand(null,filteredBrand.getName(),new ItemCategory(filteredBrand.getCategory().getId()),true,false)
        ).toList();
        return categoryBrandRepository.saveAll(cBrands);
    }

    @Transactional
    public void sendBrandToCps(Jwt token,String name, Long categoryId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(token.getTokenValue());

        orgOp.ifPresent(org->
            headers.set("orgId", org.getCpsVendorRegistrationId().toString())
        );
        Map<String,Object> data = new HashMap<>();
        data.put("brandName",name);
        data.put("subCategory",new ReferenceObjectDto(categoryId));
        HttpEntity<Map<String,Object>> payload = new HttpEntity<>(data,headers);
        String url = cpsServerConfig.getPendingItemReqEndpoint()+"/pending-brands";
        networkService.post(url,payload,Void.class);
    }

}
