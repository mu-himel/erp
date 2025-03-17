package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.internal_requisition.dto.request.ReceiveStockDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.StoreIRReqDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.TransferStockDto;
import com.agi.aesl.erpscm.internal_requisition.repository.StoreIrRepository;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

public interface IrStoreService extends VerificationDomainService {

    void submit(Jwt token, String uri, StoreIRReqDto storeIRReqDto);

    Optional<Map<String,Object>> getStoreIRDetail(Long id);

    Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                              Optional<String> fromDate, Optional<String> toDate
                               );

    void transferStock(TransferStockDto transferStockDto);

    void receiveStock(ReceiveStockDto receiveStockDto);

    void declineStock(ReceiveStockDto receiveStockDto);

    Page<StoreIrRepository.PendingStoreIR> getReadyForTransfer(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrVerification(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<StoreIrRepository.PendingStoreIR> getPendingStoreIrApproval(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<StoreIrRepository.PendingStoreIR> getReceiveStoreRequisitions(Jwt token, Optional<Integer> page, Optional<Integer> size);

    void acceptReturn(ReceiveStockDto receiveStockDto);
}
