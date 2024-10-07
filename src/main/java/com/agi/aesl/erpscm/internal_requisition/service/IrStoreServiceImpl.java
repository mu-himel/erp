package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.internal_requisition.dto.request.ReceiveStockDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.StoreIRReqDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.TransferStockDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RefDto;
import com.agi.aesl.erpscm.user_application_validation.dto.request.RejectDto;
import com.agi.aesl.erpscm.user_application_validation.entity.UserApplicationValidation;
import com.agi.aesl.erpscm.user_application_validation.repository.UserApplicationValidationRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IrStoreServiceImpl implements IrStoreService{
    @Override
    public void submit(Jwt token, String uri, StoreIRReqDto storeIRReqDto) {

    }

    @Override
    public Optional<?> getStoreIRDetail(Long id) {
        return Optional.empty();
    }

    @Override
    public Page<?> getPendingStoreIrs(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        return null;
    }

    @Override
    public void transferStock(TransferStockDto transferStockDto) {

    }

    @Override
    public void receiveStock(ReceiveStockDto receiveStockDto) {

    }

    @Override
    public void declineStock(ReceiveStockDto receiveStockDto) {

    }

    @Override
    public Page<?> getReadyForTransfer(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        return null;
    }

    @Override
    public Page<?> getPendingStoreIrVerification(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        return null;
    }

    @Override
    public Page<?> getPendingStoreIrApproval(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        return null;
    }

    @Override
    public Page<?> getReceiveStoreRequisitions(Jwt token, Optional<Integer> page, Optional<Integer> size) {
        return null;
    }

    @Override
    public void acceptReturn(ReceiveStockDto receiveStockDto) {

    }

    @Override
    public void onVerify(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextVerifier) {

    }

    @Override
    public void onApprove(Long id, UserApplicationValidation verification, UserApplicationValidationRepository.VerificationResponse nextApprover) {

    }

    @Override
    public void verifyComplete(Long id, Optional<UserApplicationValidationRepository.VerificationResponse> firstApprover) {

    }

    @Override
    public void approveComplete(Long id) {

    }

    @Override
    public void sendForReview(Long domainId, RefDto reviewer, String comment) {

    }

    @Override
    public void onRejected(Employee verifier, Long domainId, RejectDto rejectDto) {

    }
}
