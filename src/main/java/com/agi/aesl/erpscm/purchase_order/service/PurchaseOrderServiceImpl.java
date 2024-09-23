package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.comment.enums.DomainType;
import com.agi.aesl.erpscm.comment.service.CommentService;
import com.agi.aesl.erpscm.common.enums.DeliveryCharge;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.inventory.entity.Item;
import com.agi.aesl.erpscm.inventory.service.ItemService;
import com.agi.aesl.erpscm.price_quotation.entity.PqTermsAndCondition;
import com.agi.aesl.erpscm.price_quotation.repository.PqTermAndConditionRepository;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.agi.aesl.erpscm.purchase_order.repository.PoGroupRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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
    public void reviewPo(Jwt token, Long id, NoteDto noteDto) {

    }

    @Override
    public void rejectPo(Jwt loggedInUser, Long id, NoteDto noteDto) {

    }
}
