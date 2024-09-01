package com.agi.aesl.erpscm.rfq.service.service;

import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.service.WarehouseService;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.entity.IndentDeliveryDetail;
import com.agi.aesl.erpscm.indent.enums.RfqStatus;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.rfq.dto.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private WarehouseService warehouseService;

    @Override
    @Transactional
    public void createRfq(Jwt token, RfqRequestDto requestDto) {
        claimResolver.setToken(token);
        Optional<Indent> indentOp = indentRepository.findById(requestDto.getId());

        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Indent not found");
        }

        Indent indent = indentOp.get();
        if(indent.getRfqStatus()!=null && !indent.getRfqStatus().equals(RfqStatus.INIT)){
            throw new RuntimeException("Sorry! this rfq already sent");
        }

        LocalDateTime currenDateTime = LocalDateTime.now();
        String rfqUuid = UUID.randomUUID().toString();
        indent.setRfqUuid(rfqUuid);
        indent.setRfqDays(requestDto.getDuration());
        indent.setSentDate(currenDateTime);
        indent.setExpireDateTime(currenDateTime.plusDays(indent.getRfqDays()));
        indent.setRfqStatus(RfqStatus.OPEN);
        indent.setIndentDetails(indent.getIndentDetails().stream().map((indentDetail)->{
            Optional<RfqItemDto> rfqItemDtoOp = requestDto.getItems().stream().filter(item->item.getId().equals(indentDetail.getId()))
                    .findFirst();
            if(rfqItemDtoOp.isPresent()){

                List<IndentDeliveryDetail> deliveryDetails = new ArrayList<>();
                rfqItemDtoOp.get().getWarehouses().stream().forEach(w->{
                    Optional<IndentDeliveryDetail> iddOp = indentDetail.getWarehouses().stream().filter(_w ->{
                        return _w.getId().equals(w.getId());
                    }).findFirst();
                    if(iddOp.isPresent()){
                        IndentDeliveryDetail idd = iddOp.get();
                        idd.setRfqQty(w.getRfqQty());
                        deliveryDetails.add(idd);
                    }
                });
                indentDetail.setWarehouses(deliveryDetails);
            }

            return indentDetail;
        }).collect(Collectors.toList()));

        // Processing CPS Tender Creation
        TenderRequestDto tenderRequestDto = new TenderRequestDto();
        ItemCategory subCategory = indent.getSubCategory();

        tenderRequestDto.setItemCategoryCode(subCategory.getCode());
        tenderRequestDto.setCode(rfqUuid);
        tenderRequestDto.setRfqNo(indent.getIndentNo());
        tenderRequestDto.setDeadline(indent.getExpireDateTime().toString());
        tenderRequestDto.setTenderItems(requestDto.getItems().stream().map(item->{
            TenderItemDto tenderItemDto = new TenderItemDto();
            tenderItemDto.setOrderQuantity(item.getOrderQty());
            // AtomicLong totalOrderQty = new AtomicLong();
            tenderItemDto.setDeliveryDetails(item.getWarehouses().stream().map(w->{
                Optional<Warehouse> warehosueOp = warehouseService.getWarehouse(w.getWarehouseId());
                if(warehosueOp.isEmpty()){
                    throw new RuntimeException("Sorry! Warehouse not found");
                }

                TenderItemDeliveryDetail tid = new TenderItemDeliveryDetail();
                // totalOrderQty.addAndGet(w.getOrderQty());
                tid.setWarehouseId(warehosueOp.get().getId());
                tid.setWareHouseName(warehosueOp.get().getName());
                tid.setWareHouseAddress(warehosueOp.get().getLocation());
                tid.setDeliveryOrderQTY(w.getRfqQty());
                return tid;
            }).collect(Collectors.toList()));

            // tenderItemDto.setOrderQuantity(totalOrderQty.get());
            tenderItemDto.setSpecification("Must be in a good condition");
            tenderItemDto.setProductDescription(item.getAttribute());
            tenderItemDto.setBrandName(item.getBrandName());
            return tenderItemDto;
        }).collect(Collectors.toList()));

        createTender(tenderRequestDto);
    }

    private void createTender(TenderRequestDto tender){
        HttpHeaders headers = new HttpHeaders();
        Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
        if(orgOp.isPresent()){
            headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
        }
        HttpEntity<TenderRequestDto> payload = new HttpEntity<>(tender,headers);
        String url = cpsConfig.getTenderEndpoint();
        ResponseEntity<?> response = networkService.post(url, payload, Void.class);
        if(response.getStatusCode()!=HttpStatus.CREATED){
            throw new RuntimeException("Tender Unable to send to CPS");
        }
    }

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
