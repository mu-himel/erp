package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.internal_requisition.dto.request.ReceiveStockDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.StoreIRReqDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.TransferStockDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface IrStoreService extends VerificationDomainService {

    void submit(Jwt token, String uri, StoreIRReqDto storeIRReqDto);

    Optional<?> getStoreIRDetail(Long id);

    Page<?> getPendingStoreIrs(Jwt token, Optional<Integer> page, Optional<Integer> size);

    void transferStock(TransferStockDto transferStockDto);

    void receiveStock(ReceiveStockDto receiveStockDto);

    void declineStock(ReceiveStockDto receiveStockDto);

    Page<?> getReadyForTransfer(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingStoreIrVerification(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingStoreIrApproval(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getReceiveStoreRequisitions(Jwt token, Optional<Integer> page, Optional<Integer> size);

    void acceptReturn(ReceiveStockDto receiveStockDto);
}
