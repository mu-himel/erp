package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import com.agi.aesl.erpscm.config.CpsServerConfig;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.network.NetworkService;
import com.agi.aesl.erpscm.organization.entity.Organization;
import com.agi.aesl.erpscm.organization.service.OrgService;
import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import com.agi.aesl.erpscm.price_quotation.repository.PqTermAndConditionRepository;
import com.agi.aesl.erpscm.purchase_order.dto.request.PoRemoteDetailReqDto;
import com.agi.aesl.erpscm.purchase_order.dto.request.PoRemoteReqDto;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PoVerificationApprovalHistory;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.purchase_order.repository.PoGroupRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PoVaHistoryRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PoGroupRepository poGroupRepository;

    @Autowired
    private UserApplicationValidatorService<PoGroup> verificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PqTermAndConditionRepository pqTermAndConditionRepository;

    @Autowired
    private ItemService itemService;
    List<PqTermsAndCondition> termsAndConditions = new ArrayList<>();

    @Autowired
    private PoVaHistoryRepository poVaHistoryRepository;

    @Autowired
    private OrgService orgService;
    
    @Autowired
    private NetworkService networkService;

    @Autowired
    private CpsServerConfig cpsServerConfig;


    @Override
    @Transactional
    public void createPurchaseOrder(List<PurchaseOrder> purchaseOrders) {
        purchaseOrderRepository.saveAll(purchaseOrders);
    }

    @Override
    public Page<?> getPendingPOs(Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return purchaseOrderRepository.findAllPendingPOs(pageable);
    }

    @Override
    public Page<?> getPendingVerificationPOs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return purchaseOrderRepository.findAllPendingVerificationPOs(
                claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<?> getPendingApprovalPOs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        claimResolver.setToken(token);
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return purchaseOrderRepository.findAllPendingApprovalPOs(
                claimResolver.getUserId(),
                pageable);
    }

    @Override
    public Page<?> getApprovedPOs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0),size.orElse(PAGE_SIZE));
        return purchaseOrderRepository.findAllApprovedPos(pageable);
    }

    @Override
    public Page<?> getClosedPOs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        Pageable pageable = PageRequest.of(page.orElse(0), size.orElse(PAGE_SIZE));
        return purchaseOrderRepository.findAllClosedPOs(pageable);
    }

    @Override
    public Map<String, Object> getPurchaseOrderDetail(Long csId) {
        // Need to update here cause now po verifing based on collection of po
        // first find poGroup from csId
        Optional<PoGroup> poGroupOp = poGroupRepository.findByCsId(csId);
        Map<String,Object> map = new HashMap<>();
        if(poGroupOp.isPresent()){
            PoGroup poGroup = poGroupOp.get();
            // then get list of PO and its child using pogroup id

            List<PurchaseOrderRepository.PurchaseOrderDetailInfo> purchaseOrders = purchaseOrderRepository.findAllByPoGroupId(poGroup.getId());
            List<Map<String,Object>> polist = new ArrayList<>();

            List<UserApplicationValidationRepository.VerificationResponse> verifiers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> approvers = new ArrayList<>();
            List<UserApplicationValidationRepository.VerificationResponse> vrs = verificationService
                    .getVerificationsByDomainTypeAndDomainId(DomainType.PO, poGroup.getId());
            vrs.stream().forEach(verifier->{
                if(verifier.getIsApproval()==false){
                    verifiers.add(verifier);
                }else{
                    approvers.add(verifier);
                }
            });

            List<?> comments = commentService.getCommentsByDomain(DomainType.PO, poGroup.getId());
            Cs cs = poGroup.getCs();
            Indent indent = cs.getIndent();
            map.put("nextVerifierId", poGroup.getNextApproverId());
            map.put("nextApproverId", poGroup.getNextApproverId());
            map.put("reviewerId", poGroup.getReviewerId());
            map.put("declineNote", poGroup.getDeclineNote());
            map.put("reviewDate",poGroup.getReviewDate());
            map.put("status", poGroup.getStatus());
            map.put("csNo",cs.getCsNo());
            map.put("csId",cs.getId());
            map.put("poGroupId",poGroup.getId());
            map.put("indentId",indent.getId());

            map.put("verifiers", verifiers);
            map.put("approvers", approvers);
            map.put("comments", comments);

            // Indent indent = poGroup.getCs().getIndent();
            map.put("categories",indent.getCategory().getId()+","+indent.getSubCategory().getId());
            map.put("categoryName", indent.getCategory().getName()+"-"+indent.getSubCategory().getName());

            polist = purchaseOrders.stream().map(po->{
                Map<String,Object> poItem = new HashMap<>();
                List<Map<String,Object>> poDetailList = new ArrayList<>();
                List<PurchaseOrderRepository.PqDetailInfo> pqDetailInfo = purchaseOrderRepository.getPurchaseOrderDetail(po.getId());
                poItem.put("poNo", po.getPoNo());
                poItem.put("date", po.getPoDate());

                pqDetailInfo.stream().forEach(pqDetail->{
                    // System.out.println(indent.getSubCategory().getCode());
                    // System.out.println(indent.getSubCategory().getId());

                    Map<String,Object> detailMap = new HashMap<>();
                    String[] summary = pqDetail.getSummary().split(",");

                    termsAndConditions = pqTermAndConditionRepository.findAllByVendorIdAndPriceQuotationId(Long.parseLong(summary[1]),Long.parseLong(summary[16]));

                    String itemAttributeToMatch = (summary[12].trim()!="")? summary[10].trim()+" - "+ summary[12].trim() : summary[10].trim();
                    Optional<Item> itemOp = itemService.getByBrandAndAttributeName(summary[11].trim(),indent.getSubCategory().getId(), itemAttributeToMatch);
                    // detailMap.put("extendedAttributes",pqDetail.get)
                    detailMap.put("orderQty",pqDetail.getOrderQty());
                    detailMap.put("totalPrice",pqDetail.getTotalPrice());
                    detailMap.put("transactionType",pqDetail.getTransactionType());

                    // detailMap.put("vendorName",summary[0]);
                    poItem.put("vendorName",summary[0]);
                    detailMap.put("vendorId",summary[1]);
                    detailMap.put("creditDays",summary[2]);
                    detailMap.put("unitPrice",summary[3]);
                    detailMap.put("estDeliveryDays",summary[4]);
                    poItem.put("deliveryCharge",summary[6].equals("1")? DeliveryCharge.INCLUDED.toString():
                            DeliveryCharge.EXCLUDED.toString());
                    poItem.put("deliveryChargeAmount",summary[7]);
                    poItem.put("vatPercent",summary[8]);
                    poItem.put("vatAmount",summary[9]);
                    detailMap.put("itemName",summary[10]);
                    detailMap.put("brandName",summary[11]);
                    detailMap.put("extendedAttribute",summary[12]);
                    detailMap.put("isItemExist",itemOp.isPresent());
                    detailMap.put("warrantyDuration",summary[13]);
                    detailMap.put("warrantyUnit",summary[14]);
                    detailMap.put("vendorType",summary[15]);
                    detailMap.put("pqId",summary[16]);
                    poDetailList.add(detailMap);
                });
                // poItem.put("vendor",po.get)
                map.put("requestedBy",po.getRequestedBy());
                poItem.put("details",poDetailList);
                poItem.put("termsAndConditions", termsAndConditions);
                return poItem;
            }).collect(Collectors.toList());

            map.put("purchaseOrders",polist);
        }
        return map;
    }

    @Override
    public void setVerificationAndApproval(Jwt token, Long csId) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findByCsId(csId);
        if(poGroupOp.isEmpty()){
            throw new RuntimeException("Sorry! Po not found");
        }

        PoGroup po = poGroupOp.get();

        Indent indent = po.getCs().getIndent();
        List<String> ids =new ArrayList<>();
        ids.add(indent.getCategory().getId().toString());
        ids.add(indent.getSubCategory().getId().toString());
//        StringBuilder sb = new StringBuilder();
//        sb.append(indent.getCategory().getId()).append(",").append(indent.getSubCategory().getId());
//
//        String categories = sb.toString();


        String uri = "scm/po";

//        @SuppressWarnings("unchecked")
//        Optional<Map<String, Object>> verifierOp = (Optional<Map<String, Object>>) verificationService.getVerifiers(loggedInUser, uri, categories);
//
//        List<Verifier> verifiers = new ArrayList<>();
//        if (verifierOp.isPresent()) {
//            Map<String, Object> verification = verifierOp.get();
//
//            verifiers = (List<Verifier>) verification.get("verifiers");
//
//            Boolean verificationRequired = (Boolean) verification.get("verificationRequired");
//            if (verificationRequired != null && verificationRequired == true && verifiers != null && verifiers.size() > 0) {
//                po.setStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
//            } else {
//                po.setStatus(PurchaseOrderStatus.PENDING);
//            }
//        }
//
//        List<ApprovalSettingQuery.ApprovalPanel> approvalPanels = approvalSettingService.getModuleWiseApprovalSetting(uri,
//                Optional.ofNullable(categories.toString()),Optional.empty());
//
//        if(po.getStatus().equals(PurchaseOrderStatus.PENDING) && approvalPanels.size()>0){
//            po.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
//        }

        // po.setStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
        po.setIsVerifyApproveEnabled(true);

        verificationService.applyVerifyApprovalProcess(po, DomainType.PO, PurchaseOrderStatus.APPROVED.toString(),
                uri,"CATEGORY",ids,null);

//        verificationService.setVerifiers(po, verifiers, DomainType.PO);
//        verificationService.setApprovers(po, approvalPanels, DomainType.PO);
    }

    @Override
    @Transactional
    public void reviewPo(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }

        PoGroup po = poGroupOp.get();
        po.setReviewerId(null);
        po.setReviewDate(LocalDateTime.now());
        if(po.getNextApproverId()!=null && po.getNextApproverId() == null){
            po.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_VERIFICATION);
        }
        if(po.getNextVerifierId()!=null && po.getNextApproverId() != null){
            po.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        }

        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),DomainType.PO,po.getId(),noteDto.getNote(),
                        noteDto.getAttachments())
        );



    }

    @Override
    @Transactional
    public void rejectPo(Jwt token, Long id, NoteDto noteDto) {
        claimResolver.setToken(token);
        Optional<PurchaseOrder> poOp = purchaseOrderRepository.findById(id);
        if(poOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }
        PurchaseOrder po = poOp.get();
        po.setStatus(PurchaseOrderStatus.REJECTED);
        commentService.addComment(
                commentService.prepareComment(claimResolver.getEmployee().get(),DomainType.PO,po.getId(),noteDto.getNote(),
                        noteDto.getAttachments())
        );
    }

    @Transactional
    private void setVAHistory(PoGroup po, PurchaseOrderStatus status){
        PoVerificationApprovalHistory poVaHistory = new PoVerificationApprovalHistory();
        poVaHistory.setPo(po);
        poVaHistory.setEmployee(new Employee(po.getNextVerifierId()));
        poVaHistory.setPoStatus(status);
        poVaHistoryRepository.save(poVaHistory);
    }

    @Override
    @Transactional
    public void onVerify(Long id, UserApplicationValidation verification,
                         UserApplicationValidationRepository.VerificationResponse nextVerifier) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup po = poGroupOp.get();
            po.setNextVerifierId(nextVerifier.getVerifier().getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.VERIFIED);
            setVAHistory(po,PurchaseOrderStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void onApprove(Long id, UserApplicationValidation verification,
                          UserApplicationValidationRepository.VerificationResponse nextApprover) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup po = poGroupOp.get();
            po.setNextApproverId(nextApprover.getVerifier().getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.APPROVED);
            setVAHistory(po, PurchaseOrderStatus.APPROVED);
        }
    }

    @Override
    @Transactional
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findById(id);
        if(poGroupOp.isPresent()){
            PoGroup poGroup = poGroupOp.get();
            if(firstApprover.isPresent()){
                poGroup.setNextApproverId(firstApprover.get().getVerifier().getId());
                poGroup.setPurchaseOrderStatus(PurchaseOrderStatus.PENDING_APPROVAL);
            }else {
                poGroup.setPurchaseOrderStatus(PurchaseOrderStatus.VERIFIED);
                sentPoToVendors(poGroup);
            }
            setVAHistory(poGroup,PurchaseOrderStatus.VERIFIED);
        }
    }

    @Override
    @Transactional
    public void approveComplete(Long id) {
        Optional<PoGroup> poGroup = poGroupRepository.findById(id);
        if(poGroup.isPresent()){
            PoGroup po = poGroup.get();
            po.setPurchaseOrderStatus(PurchaseOrderStatus.APPROVED);
            setVAHistory(po,PurchaseOrderStatus.APPROVED);
            sentPoToVendors(po);
        }
    }

    @Async
    private void sentPoToVendors(PoGroup poGroup) {
        // Replace Purchase Order Reference with Po group for this method
        // Get List of Purchase Orders and process sent po to vendor for that collection of po items
        List<PoRemoteReqDto> remotePos = new ArrayList<>();


        List<PurchaseOrderRepository.PurchaseOrderDetailInfo> purchaseOrders = purchaseOrderRepository.findAllByPoGroupId(poGroup.getId());

        for(PurchaseOrderRepository.PurchaseOrderDetailInfo po : purchaseOrders){
            PoRemoteReqDto poRemoteReqDto = new PoRemoteReqDto();
            List<PoRemoteDetailReqDto> orderDetails = new ArrayList<>();
            List<PurchaseOrderRepository.PqDetailInfo> pqDetailInfos = purchaseOrderRepository.getPurchaseOrderDetail(po.getId());
            poRemoteReqDto.setId(po.getId());
            poRemoteReqDto.setPoNo(po.getPoNo());
            System.out.println(po.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            poRemoteReqDto.setPoDate(po.getCreatedAt().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            poRemoteReqDto.setCategoryCode(po.getCs().getIndent().getSubCategory().getCode());
            poRemoteReqDto.setTenderNo(po.getCs().getIndent().getIndentNo());
            poRemoteReqDto.setDeliveryDate(po.getPoDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            pqDetailInfos.stream().forEach(pqdi->{
                if(po.getId().equals(pqdi.getPoId())){
                    PoRemoteDetailReqDto prdr = new PoRemoteDetailReqDto();
                    prdr.setItemQty(pqdi.getOrderQty());
                    String[] summary = pqdi.getSummary().split(",");
                    prdr.setItemName(summary[10]+" - "+summary[12]);
                    poRemoteReqDto.setVendorId(Long.valueOf(summary[1]));
                    poRemoteReqDto.setOfferId(Long.valueOf(summary[5]));
                    orderDetails.add(prdr);
                }

            });
            poRemoteReqDto.setOrderDetails(orderDetails);
            remotePos.add(poRemoteReqDto);
        }

        try{
            HttpHeaders headers = new HttpHeaders();
            Optional<Organization> orgOp = orgService.getOrgByCodeFromAcl(claimResolver.getToken().getTokenValue());
            if(orgOp.isPresent()){
                headers.set("orgId",orgOp.get().getCpsVendorRegistrationId().toString());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String,List<PoRemoteReqDto>> payloadMap = new HashMap<>();
            payloadMap.put("purchaseOrders",remotePos);
            HttpEntity<Map<String,List<PoRemoteReqDto>>> payload = new HttpEntity<>(payloadMap,headers);
            String url = cpsServerConfig.getSentPoEndpoint();
            ResponseEntity<Void> response = networkService.post(url, payload,Void.class);
            if(!response.getStatusCode().equals(HttpStatus.CREATED)){
                throw new RuntimeException("Sorry! Something wrong");
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {
        Optional<PoGroup> poOp  = poGroupRepository.findById(domainId);
        if(poOp.isPresent()){
            PoGroup po = poOp.get();
            po.setReviewerId(reviewer.getId());
            po.setPurchaseOrderStatus(PurchaseOrderStatus.REVIEW);
            po.setReviewDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {
        Optional<PurchaseOrder> poOp = purchaseOrderRepository.findById(domainId);
        if(poOp.isEmpty()){
            throw new RuntimeException("Sorry! PO not found");
        }
        PurchaseOrder po = poOp.get();
        po.setStatus(PurchaseOrderStatus.REJECTED);
    }
}
