package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.utils.ClaimResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService{
    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private ClaimResolver claimResolver;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

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

    }

    @Override
    public void reviewPo(Jwt token, Long id, NoteDto noteDto) {

    }

    @Override
    public void rejectPo(Jwt loggedInUser, Long id, NoteDto noteDto) {

    }
}
