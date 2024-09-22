package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.repository.PoGroupRepository;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PoGroupRepository poGroupRepository;

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
        return null;
    }

    @Override
    public void setVerificationAndApproval(Jwt token, Long csId) {
        Optional<PoGroup> poGroupOp = poGroupRepository.findByCsId(csId);
        if(poGroupOp.isEmpty()){
            throw new RuntimeException("Sorry! Po not found");
        }

        PoGroup po = poGroupOp.get();

        Indent indent = po.getCs().getIndent();
        StringBuilder sb = new StringBuilder();
        sb.append(indent.getCategory().getId()).append(",").append(indent.getSubCategory().getId());

        String categories = sb.toString();


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
