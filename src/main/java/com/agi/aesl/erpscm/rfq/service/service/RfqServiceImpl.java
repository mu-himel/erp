package com.agi.aesl.erpscm.rfq.service.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.rfq.dto.AvailableVendorCount;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RfqServiceImpl implements RfqService{

    private static final Integer PAGE_SIZE = 20;
    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private OrgService orgService;

    @Autowired
    private CpsServerConfig cpsConfig;

    @Autowired
    private NetworkService networkService;

    @Override
    public Page<?> getAllPendingRFQs(Jwt token, Optional<String> indentNo, Optional<String> category,
                                     Optional<String> subCategory, Optional<String> priority,
                                     Optional<Integer> daysRemain, Optional<String> fromDateOp,
                                     Optional<String> toDateOp, Optional<Integer> page, Optional<Integer> size) {

        claimResolver.setToken(token);
        String uri="";
        // add data filter

        if((fromDateOp.isPresent() && toDateOp.isEmpty()) ||
                (fromDateOp.isEmpty() && toDateOp.isPresent())){
            throw new RuntimeException("Please select both date filter");
        }
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent() && toDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+"T00:00:00");
            toDate   = LocalDateTime.parse(fromDateOp.get()+"T23:59:59");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);

        return indentRepository.getAllApprovedIndents(
                indentNo.orElse(null),
                category.orElse(null),
                subCategory.orElse(null),
                priority.orElse(null),
                daysRemain.orElse(null),
                fromDate,
                toDate,
                pageable);
    }

    @Override
    public Page<?> getAllSentRfqs(Jwt token, Optional<String> indentNo, Optional<String> category,
                                  Optional<String> subCategory, Optional<String> priority, Optional<Integer> daysRemain,
                                  Optional<String> fromDateOp, Optional<String> toDateOp, Optional<Integer> page,
                                  Optional<Integer> size) {

        claimResolver.setToken(token);
        String uri="";

        if((fromDateOp.isPresent() && toDateOp.isEmpty()) ||
                (fromDateOp.isEmpty() && toDateOp.isPresent())){
            throw new RuntimeException("Please select both date filter");
        }
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;
        if(fromDateOp.isPresent() && toDateOp.isPresent()){
            fromDate = LocalDateTime.parse(fromDateOp.get()+"T00:00:00");
            toDate   = LocalDateTime.parse(fromDateOp.get()+"T23:59:59");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE), sort);
        return indentRepository.getAllIndentsWithOpenRfqStatus(
                indentNo.orElse(null),
                category.orElse(null),
                subCategory.orElse(null),
                priority.orElse(null),
                daysRemain.orElse(null),
                fromDate,
                toDate,
                pageable);
    }

    @Override
    public Optional<?> getAvailableVendorsCount(Jwt token, Long id) {
        Optional<Indent> indentOp = indentRepository.findById(id);

        claimResolver.setToken(token);

        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Indent not found");
        }

        ItemCategory subCategory = indentOp.get().getSubCategory();
        if(subCategory==null){
            throw new RuntimeException("Sorry! Indent's Sub Category not found");
        }

        return getVendorCount(subCategory.getCode());
    }

    private Optional<?> getVendorCount(String subCatCode){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }
        System.out.println(cpsConfig.getVendorCountEndpoint(subCatCode));
        HttpEntity<?> payload = new HttpEntity<>(headers);
        ResponseEntity<AvailableVendorCount> resposne = networkService.get(
                cpsConfig.getVendorCountEndpoint(subCatCode),
                payload,
                AvailableVendorCount.class
        );
        if(resposne.getStatusCode().equals(HttpStatus.OK)){
            return Optional.ofNullable(resposne.getBody());
        }
        return Optional.empty();
    }
}
