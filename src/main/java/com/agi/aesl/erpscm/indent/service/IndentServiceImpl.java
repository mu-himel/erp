package com.agi.aesl.erpscm.indent.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.DataFilter;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.erpn_integration.service.IntegrationReaderService;
import com.agi.aesl.erpscm.indent.dto.request.IndentRequestDto;
import com.agi.aesl.erpscm.indent.dto.request.MoveIndentRequestDto;
import com.agi.aesl.erpscm.indent.entity.*;
import com.agi.aesl.erpscm.indent.enums.IndentStatus;
import com.agi.aesl.erpscm.indent.enums.IndentVerificationStatus;
import com.agi.aesl.erpscm.indent.enums.RfqStatus;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.indent.repository.IndentVerificationApprovalRepository;
import com.agi.aesl.erpscm.inventory.entity.ItemCategory;
import com.agi.aesl.erpscm.pr_indent.repository.PrIndentRepository;
import com.agi.aesl.erpscm.price_quotation.dto.request.PriceQuotationReqDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.dto.response.AppliedVADto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class IndentServiceImpl implements IndentService{

    @Autowired
    private IndentRepository indentRepository;

    @Autowired
    private PrIndentRepository prIndentRepository;

    @Autowired
    private IndentVerificationApprovalRepository indentVARepository;

    @Autowired
    private UserApplicationValidatorService<Indent> verificationService;

    @Autowired
    private IntegrationReaderService integrationReaderService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ClaimResolver claimResolver;

    private final Integer PAGE_SIZE = 10;

    @Override
    public String getNextIndentNo() {
        Optional<Long> demandOptional = indentRepository.findMaxIndentById();
        if (demandOptional.isPresent()) {
            Long demandNo = demandOptional.get();
            Long newDemandNo = demandNo + 1L;
            return String.format("%06d", newDemandNo);
        }
        return String.format("%06d", 1);
    }

    @Transactional
    private void setIndentDetail(Indent indent, IndentRequestDto indentRequestDto, List<String> ids, Boolean isNew){
        indent.setIndentDetails(indentRequestDto.getItems().stream().map(item->{

            IndentDetail indentDetail = new IndentDetail((!isNew)?item.getId():null);
            indentDetail.setIndent(indent);
            indentDetail.setProductRequirementsIds(item.getProductRequirementsIds());
            indentDetail.setItemAttribute(item.getAttribute());
            indentDetail.setBrandId(item.getBrandId());
            indentDetail.setSubCategory(new ItemCategory(item.getSubCategoryId()));
            ids.add(item.getSubCategoryId().toString());


            indentDetail.setWarehouses(item.getWarehouses().stream().map(w->{
                IndentDeliveryDetail idd = new IndentDeliveryDetail((!isNew)?w.getId():null);
                idd.setRfqQty(w.getRfqQty());
                idd.setWarehouse(new Warehouse(w.getWarehouseId()));
                idd.setIndentDetail(indentDetail);
                idd.setOrderQty(w.getOrderQty());
                idd.setPrQty(w.getPrQty());

                idd.setPartialDeliveries(w.getPartialDeliveries().stream().map(pd->{
                    IndentPartialDelivery ipd = new IndentPartialDelivery((!isNew)?pd.getId():null);
                    ipd.setPdDate(pd.getPdDate());
                    ipd.setQty(pd.getQty());
                    ipd.setIndentDeliveryDetail(idd);
                    return ipd;
                }).collect(Collectors.toList()));
                return idd;
            }).collect(Collectors.toList()));

            return indentDetail;
        }).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void createIndent(Jwt token, String uri, IndentRequestDto indentRequestDto) {
        claimResolver.setToken(token);
        List<String> ids = new ArrayList<>();
        if(indentRequestDto.getIndentNo()==null || indentRequestDto.getIndentNo().trim().length()<=0){
            throw new RuntimeException("Sorry! Indent No Required");
        }
        ids.add(indentRequestDto.getCategoryId().toString());

        Indent indent = indentRequestDto.getEntity();

        if(claimResolver.getEmployee().isPresent()) {
            indent.setWarehouse(new Warehouse(claimResolver.getEmployee().get().getWarehouseId()));
        }
        if(indentRequestDto.getIsDevliverToSingleWarehouse()){
            indent.setIsDevliverToSingleWarehouse(true);
            indent.setSingleWarehouse(new Warehouse(indentRequestDto.getSingleWarehouse().getId()));
        }

        indent.setCategory(new ItemCategory(indentRequestDto.getCategoryId()));
        indent.setSubCategory(new ItemCategory(indentRequestDto.getSubCategoryId()));
        indent.setIndentNo(indentRequestDto.getIndentNo());
        if(claimResolver.getEmployee().isPresent()) {
            indent.setRequestedBy(new Employee(claimResolver.getEmployee().get().getId()));
        }
//        indent.setPriority(IndentPriority.valueOf(indentRequestDto.getPriority()));
        indent.setPriorityDateTime(indentRequestDto.getPriorityDate());

        setIndentDetail(indent,indentRequestDto,ids,true);
        indent.setIstatus(IndentStatus.INIT);

        List<String> prids = indentRequestDto.getPrIds().stream().map(prid->{
            return prid.toString();
        }).toList();
        indent.setPrIndents(String.join(",",prids));

        indentRepository.save(indent);
        verificationService.removeVerification(indent.getId(), DomainType.INDENT);

        AppliedVADto appliedVa = verificationService.applyVerifyApprovalProcess(indent, DomainType.INDENT, IndentVerificationStatus.APPROVED.toString(),
                uri, "CATEGORY", ids, null);

        if(appliedVa.getVerifiers().isEmpty() && appliedVa.getPanels().isEmpty()){
            indent.setRfqStatus(RfqStatus.INIT);
            indent.setIndentStatus(IndentVerificationStatus.COMPLETED);
        }

        indentVARepository.deleteAllByIndentId(indent.getId());
        prIndentRepository.updatePrIndentToClose(indentRequestDto.getPrIds());
    }

    @Override
    @Transactional
    public void updateIndent(Jwt token, String uri, Long id, IndentRequestDto indentRequestDto) {
        claimResolver.setToken(token);
        List<String> ids = new ArrayList<>();
        ids.add(indentRequestDto.getCategoryId().toString());

        Optional<Indent> indentOp = indentRepository.findById(id);
        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Indent not found");
        }
        Indent indent = indentOp.get();


        if(indentRequestDto.getIsDevliverToSingleWarehouse()){
            indent.setIsDevliverToSingleWarehouse(true);
            indent.setSingleWarehouse(new Warehouse(indentRequestDto.getSingleWarehouse().getId()));
        }
        indent.setPriorityDateTime(indentRequestDto.getPriorityDate());
        setIndentDetail(indent,indentRequestDto,ids,false);
        indentRepository.save(indent);
        verificationService.removeVerification(indent.getId(),DomainType.INDENT);
        indentVARepository.deleteAllByIndentId(indent.getId());
        verificationService.applyVerifyApprovalProcess(indent,DomainType.INDENT,
                IndentVerificationStatus.APPROVED.toString(),uri,"CATEGORY",ids,null);
        List<Long> prids = Arrays.stream(indent.getPrIndents().split(",")).map(Long::parseLong).toList();
        prIndentRepository.updatePrIndentToClose(prids);
        indent.setReviewerId(null);
        indent.setReviewPrevStatus(null);
        indent.setReviewDate(null);

    }

    @Override
    public Page<?> getAllIndents(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId,
                                 Optional<Long> subCategoryId, Optional<String> priority) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();

        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllIndents(categoryIds,warehouseIds,pageable);
    }

    @Override
    public Page<?> getAllPendingVerificationIndents(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId,
                                 Optional<String> priority, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri="";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();
        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllPendingVerifications(
                claimResolver.getUserId(),
                categoryIds,warehouseIds,pageable);
    }

    @Override
    public Page<?> getAllPendingApprovalIndents(Jwt token, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<String> priority, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        String uri = "";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();
        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllPendingApprovals(claimResolver.getUserId(),
                categoryIds,warehouseIds,pageable);
    }

    @Override
    public Page<?> getAllClosedIndents(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<Long> categoryId, Optional<Long> subCategoryId, Optional<String> priority) {
        claimResolver.setToken(token);
        String uri = "";
        Sort sort = Sort.by(Sort.Direction.DESC,"id");
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE), sort);
        DataFilter dataFilter = new DataFilter(uri,claimResolver);
        dataFilter.setReaderService(integrationReaderService);
        List<Long> warehouseIds = dataFilter.getFilterConfig();
        List<Long> categoryIds = dataFilter.getCategoryIds();
        categoryId.ifPresent(categoryIds::add);
        subCategoryId.ifPresent(categoryIds::add);
        return indentRepository.getAllClosedIndents(categoryIds,warehouseIds,pageable);
    }

    @Override
    public Map<String, Object> getIndentDetailById(Long indentId) {
        Optional<Indent> indentOptional = indentRepository.findById(indentId);
        List<IndentRepository.IndentViewInfo> result = indentRepository.getIndentById(indentId);

        if(indentOptional.isPresent() && !result.isEmpty()){
            Map<String, Object> response = new HashMap<>();

            Indent indent = indentOptional.get();
            prepareDetail(indent,result,response);
            return response;

        }

        return null;
    }

    private void prepareDetail(Indent indent, List<IndentRepository.IndentViewInfo> result, Map<String, Object> response){
        Warehouse warehouse = indent.getSingleWarehouse();
        Boolean isDeliverInSingleWarehouse = indent.getIsDevliverToSingleWarehouse();
        response.put("indentId",indent.getId());
        response.put("indentNo",indent.getIndentNo());
        response.put("requestedBy",indent.getRequestedBy());
        response.put("categoryId", indent.getCategory().getId());
        response.put("categoryName", indent.getCategory().getName());
        response.put("vatPercent", indent.getSubCategory().getVat());
        response.put("subCategoryId",indent.getSubCategory().getId());
        response.put("subCategoryName",indent.getSubCategory().getName());
        response.put("reviewerId",indent.getReviewerId());
        response.put("isDevliverToSingleWarehouse", isDeliverInSingleWarehouse!=null? isDeliverInSingleWarehouse:false);
        response.put("singleWarehouseId",(warehouse!=null)? warehouse.getId(): null);
        response.put("singleWarehouseName",(warehouse!=null)? warehouse.getName(): null);
        response.put("indentDetails", getProcessedResult(result));
        response.put("indentDate",indent.getIndentDate());
        response.put("expireDateTime",indent.getExpireDateTime());
        response.put("status",indent.getIndentStatus());
        response.put("warehouse",indent.getWarehouse());
        List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
        List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                .getVerificationsByDomainTypeAndDomainId(DomainType.INDENT, indent.getId());
        vrs.stream().forEach(verifier->{
            if(verifier.getIsApproval()==false){
                verifiers.add(verifier);
            }else{
                approvers.add(verifier);
            }
        });

        List<?> comments = commentService.getCommentsByDomain(DomainType.INDENT, indent.getId());

        response.put("verifiers",verifiers);
        response.put("approvers",approvers);
        response.put("comments",comments);

    }

    private List<?> getProcessedResult(List<IndentRepository.IndentViewInfo> result){
        List<Map<String,Object>> items = new ArrayList<>();
        AtomicReference<Long> prQty= new AtomicReference<>(0L);
        result.stream().forEach(indentViewInfo->{
            Map<String,Object> item = new HashMap<>();
            String warehouseKey = indentViewInfo.getBrandName()+"_"+indentViewInfo.getItemName()+"_"+indentViewInfo.getWarehouseId();

            Optional<Map<String,Object>> anyItemOp = items.stream().filter(_item->{
                return _item.get("brandName").equals(indentViewInfo.getBrandName()) && _item.get("itemName").equals(indentViewInfo.getItemName());
            }).findAny();

            if(anyItemOp.isEmpty()){
                item.put("id", indentViewInfo.getDetailId());
                item.put("productRequirementsIds",indentViewInfo.getProductRequirementIds());
                item.put("prDetailId",indentViewInfo.getDetailId());


                prQty.getAndUpdate(v -> v + indentViewInfo.getPrQty());

                item.put("prQty",prQty);

                item.put("itemName",indentViewInfo.getItemName());
                item.put("categoryName", indentViewInfo.getCategoryName());
                item.put("subCategoryName", indentViewInfo.getSubCategoryName());
                item.put("categoryId", indentViewInfo.getCategoryId());
                item.put("subCategoryId", indentViewInfo.getSubCategoryId());
                item.put("daysRemain", indentViewInfo.getDaysRemain());
                item.put("priority",indentViewInfo.getPriority());
                item.put("priorityDate",indentViewInfo.getPriorityDate());
                item.put("brandId", indentViewInfo.getBrandId());
                item.put("brandName",indentViewInfo.getBrandName());
                Map<String,Object> warehouseInfo = new HashMap<>();
                warehouseInfo.put("id", indentViewInfo.getPiwId());
                warehouseInfo.put("warehouseId",indentViewInfo.getWarehouseId());
                warehouseInfo.put("warehouseName",indentViewInfo.getWarehouseName());
                warehouseInfo.put("orderQty", indentViewInfo.getOrderQty());
                warehouseInfo.put("prQty", indentViewInfo.getPrQty());
                warehouseInfo.put("rfqQty", indentViewInfo.getRfqQty());
                // add key for unique check in existing block
                warehouseInfo.put("key", indentViewInfo.getDetailId()+"_"+indentViewInfo.getPiwId());

                List<Map<String,Object>> pdList = new ArrayList<>();
                Map<String,Object> pd = new HashMap<>();
                pd.put("id",indentViewInfo.getPdId());
                pd.put("pdDate",indentViewInfo.getPdDate());
                pd.put("qty",indentViewInfo.getPdQty());
                pdList.add(pd);
                if(indentViewInfo.getPdDate()!= null && indentViewInfo.getPdQty()!=null){
                    warehouseInfo.put("partialDeliveries",pdList);
                }else{
                    warehouseInfo.put("partialDeliveries", new ArrayList<>());
                }

                Map<String,Object> warehousKeyMap = new HashMap<>();

                warehousKeyMap.put(warehouseKey, warehouseInfo);
                item.put("warehouses",warehousKeyMap);

                items.add(item);
            }else{
                Map<String,Object> existItem = (Map<String,Object>)anyItemOp.get();
                prQty.getAndUpdate(v -> v + indentViewInfo.getPrQty());
                existItem.put("prQty",prQty);
                if(existItem.containsKey("warehouses")){
                    Map<String,Object> existWarehouseProp = (Map<String,Object>)existItem.get("warehouses");

                    if(existWarehouseProp.containsKey(warehouseKey)){
                        Map<String,Object> warehousKeyMap = (Map<String,Object>)existWarehouseProp.get(warehouseKey);

                        String key = (String)warehousKeyMap.get("key");
                        String currentkey = indentViewInfo.getDetailId()+"_"+indentViewInfo.getPiwId();

                        if(!key.contains(currentkey))
                        {
                            BigDecimal orderQty = (BigDecimal)warehousKeyMap.get("orderQty");
                            orderQty =indentViewInfo.getOrderQty().add(orderQty);
                            warehousKeyMap.replace("orderQty",orderQty);
                            Long _prQty = (Long) warehousKeyMap.get("prQty");
                            _prQty += indentViewInfo.getPrQty();
                            warehousKeyMap.replace("prQty",_prQty);

                            warehousKeyMap.replace("key", currentkey);
                        }

                        List<Map<String,Object>> pdList = (List<Map<String,Object>>)warehousKeyMap.get("partialDeliveries");
                        Map<String,Object> pd = new HashMap<>();
                        pd.put("id",indentViewInfo.getPdId());
                        pd.put("pdDate",indentViewInfo.getPdDate());
                        pd.put("qty",indentViewInfo.getPdQty());
                        pdList.add(pd);
                    }else{
                        Map<String,Object> warehouseInfo = new HashMap<>();
                        warehouseInfo.put("id", indentViewInfo.getPiwId());
                        warehouseInfo.put("warehouseId",indentViewInfo.getWarehouseId());
                        warehouseInfo.put("warehouseName",indentViewInfo.getWarehouseName());
                        warehouseInfo.put("orderQty", indentViewInfo.getOrderQty());
                        warehouseInfo.put("prQty", indentViewInfo.getPrQty());
                        warehouseInfo.put("rfqQty", indentViewInfo.getRfqQty());
                        // add key for unique check in existing block
                        warehouseInfo.put("key", indentViewInfo.getDetailId()+"_"+indentViewInfo.getPiwId());

                        List<Map<String,Object>> pdList = new ArrayList<>();
                        Map<String,Object> pd = new HashMap<>();
                        pd.put("id",indentViewInfo.getPdId());
                        pd.put("pdDate",indentViewInfo.getPdDate());
                        pd.put("qty",indentViewInfo.getPdQty());
                        pdList.add(pd);
                        if(indentViewInfo.getPdDate()!= null && indentViewInfo.getPdQty()!=null){
                            warehouseInfo.put("partialDeliveries",pdList);
                        }else{
                            warehouseInfo.put("partialDeliveries", new ArrayList<>());
                        }

//                        Map<String,Object> warehousKeyMap = new HashMap<>();

                        existWarehouseProp.put(warehouseKey, warehouseInfo);
                        existItem.put("warehouses",existWarehouseProp);
                    }
                }
            }
        });

        List<?> fitems = items.stream().map(_item->{
            Map<String,Object> warehouses = (Map<String,Object>)_item.get("warehouses");
            _item.replace("warehouses", warehouses.values());
            return _item;
        }).collect(Collectors.toList());

        return fitems;
    }

    @Override
    public Optional<Indent> getIndentByCode(String code) {
        return indentRepository.findByRfqUuid(code);
    }

    @Override
    public Optional<Indent> getIndentById(Long id) {

        return indentRepository.findById(id);
    }

    @Override
    public List<?> getIndentByIds(Optional<List<Long>> indentIds) {
        List<?> results = indentRepository.getIndentByIds(
                indentIds.orElseThrow(()->new RuntimeException("Indent ids should not empty"))
        );
        return results;
    }

    @Override
    @Transactional
    public int moveIndentByIds(MoveIndentRequestDto moveIndent) {
        if (CollectionUtils.isEmpty(moveIndent.getIds())) {
            throw new RuntimeException("Indents should not empty");
        }
        int result = indentRepository.moveIndentByIds(moveIndent.getIds());

        return result;
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<Indent> indentOp  = indentRepository.findById(id);
        if(indentOp.isPresent()){
            Indent indent = indentOp.get();
            IndentVerificationApprovalHistory indentVAHistory = new IndentVerificationApprovalHistory();
            indentVAHistory.setIndent(indent);
            indentVAHistory.setEmployee(verification.getVerifier());
            indentVAHistory.setIndentStatus(IndentVerificationStatus.VERIFIED);
            indentVARepository.save(indentVAHistory);
            indent.setNextVerifierId(nextVerifier.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<Indent> indentOp  = indentRepository.findById(id);
        if(indentOp.isPresent()){
            Indent indent = indentOp.get();
            IndentVerificationApprovalHistory indentVAHistory = new IndentVerificationApprovalHistory();
            indentVAHistory.setIndent(indent);
            indentVAHistory.setEmployee(verification.getVerifier());
            indentVAHistory.setIndentStatus(IndentVerificationStatus.APPROVED);
            indentVARepository.save(indentVAHistory);
            indent.setNextApproverId(nextApprover.getVerifier().getId());
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<Indent> indentOp  = indentRepository.findById(id);
        if(indentOp.isPresent()){
            Indent indent = indentOp.get();
            if(firstApprover.isPresent()){
                IndentVerificationApprovalHistory indentVAHistory = new IndentVerificationApprovalHistory();
                indentVAHistory.setIndent(indent);
                indentVAHistory.setEmployee(new Employee(indent.getNextVerifierId()));
                indentVAHistory.setIndentStatus(IndentVerificationStatus.VERIFIED);
                indentVARepository.save(indentVAHistory);

                indent.setNextApproverId(firstApprover.get().getVerifier().getId());
                indent.setIndentStatus(IndentVerificationStatus.PENDING_APPROVAL);

            }else {
                indent.setRfqStatus(RfqStatus.INIT);
                indent.setIndentStatus(IndentVerificationStatus.VERIFIED);
                IndentVerificationApprovalHistory indentVAHistory = new IndentVerificationApprovalHistory();
                indentVAHistory.setIndent(indent);
                indentVAHistory.setEmployee(new Employee(indent.getNextVerifierId()));
                indentVAHistory.setIndentStatus(IndentVerificationStatus.VERIFIED);
                indentVARepository.save(indentVAHistory);
            }

        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<Indent> indentOp  = indentRepository.findById(id);
        if(indentOp.isPresent()){
            Indent indent = indentOp.get();
            indent.setRfqStatus(RfqStatus.INIT);
            indent.setStatus(String.valueOf(IndentVerificationStatus.APPROVED));

            IndentVerificationApprovalHistory indentVAHistory = new IndentVerificationApprovalHistory();
            indentVAHistory.setIndent(indent);
            indentVAHistory.setEmployee(new Employee(indent.getNextApproverId()));
            indentVAHistory.setIndentStatus(IndentVerificationStatus.APPROVED);
            indentVARepository.save(indentVAHistory);
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<Indent> indentOp  = indentRepository.findById(domainId);
        if(indentOp.isPresent()){
            Indent indent = indentOp.get();
            if(!indent.getIndentStatus().equals(IndentVerificationStatus.REVIEW)){
                indent.setReviewPrevStatus(indent.getIndentStatus());
                indent.setIndentStatus(IndentVerificationStatus.REVIEW);
            }
            indent.setReviewerId(reviewer.getId());
            indent.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void reviewIndent(Jwt token, Long id, ReviewDto reviewDto) {
        claimResolver.setToken(token);
        if(claimResolver.getEmployee().isEmpty()){
            throw new RuntimeException("Sorry! Employee Profile Required");
        }

        Optional<Indent> indentOp = indentRepository.findById(id);
        if(indentOp.isEmpty()){
            throw new RuntimeException("Indent not found");
        }
        Indent indent = indentOp.get();
        indent.setReviewerId(null);
        if(indent.getReviewPrevStatus()!=null) {
            indent.setIndentStatus(indent.getReviewPrevStatus());
        }
        indent.setReviewPrevStatus(null);
        indent.setReviewDate(LocalDateTime.now());

        commentService.addComment(commentService.prepareComment(
                claimResolver.getEmployee().get(),
                reviewDto.getDomainType(),
                indent.getId(),
                reviewDto.getMessage(),
                reviewDto.getAttachments()
        ));

    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<Indent> indentOp = indentRepository.findById(domainId);
        indentOp.ifPresent(indent -> {
            indent.setIndentStatus(IndentVerificationStatus.REJECTED);
        });
    }

    @Override
    public Optional<Indent> getIndentFactory(PriceQuotationReqDto pqDto) {
        Optional<Indent> indentOp = Optional.empty();
        if(pqDto.getRfqId() != null && pqDto.getCode() == null){
            indentOp = this.getIndentById(pqDto.getRfqId());
        }

        if(pqDto.getCode() != null && pqDto.getRfqId() == null){
            indentOp = this.getIndentByCode(pqDto.getCode());
        }

        if(indentOp.isEmpty()){
            throw new RuntimeException("Sorry! Rfq not found");
        }
        return indentOp;
    }
}
