package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.purchase_order.dto.request.PurchaseRequestDto;
import com.agi.aesl.erpscm.purchase_order.entity.PoGroup;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.repository.PurchaseOrderRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseOrderService extends VerificationDomainService {
    Page<PurchaseOrderRepository.PendingPOItemDetail> getPendingPOs(Optional<String>vendor, Optional<String>csNo,
                                                                    Optional<String> poNo,
                                                                    Optional<Long> categoryId,
                                                                    Optional<Long> subCategoryId,
                                                                    Optional<String> fromDateStr,
                                                                    Optional<String> toDateStr,
                                                                    Optional<String> status,
                                                                    Optional<Integer> page, Optional<Integer> size);

    Page<PurchaseOrderRepository.PendingPOItemDetail> getPendingVerificationPOs(Jwt token,
                                                                                Optional<String>vendor, Optional<String>csNo,
                                                                                Optional<String> poNo, Optional<Long> categoryId,
                                                                                Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                                                                Optional<String> toDateStr, Optional<String> status,
                                                                                Optional<Integer> page, Optional<Integer> size);

    Page<PurchaseOrderRepository.PendingPOItemDetail> getPendingApprovalPOs(Jwt token,
                                                                            Optional<String>vendor, Optional<String>csNo,
                                                                            Optional<String> poNo, Optional<Long> categoryId,
                                                                            Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                                                            Optional<String> toDateStr, Optional<String> status,
                                                                            Optional<Integer> page, Optional<Integer> size);

    Page<PurchaseOrderRepository.ClosedPOListItem> getApprovedPOs(Jwt token,
                                                                  Optional<String>vendor, Optional<String>csNo,
                                                                  Optional<String> poNo, Optional<Long> categoryId,
                                                                  Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                                                  Optional<String> toDateStr, Optional<String> status,
                                                                  Optional<Integer> page, Optional<Integer> size);

    Page<PurchaseOrderRepository.PendingPOItemDetail> getClosedPOs(Jwt token,
                                                                   Optional<String>vendor, Optional<String>csNo,
                                                                   Optional<String> poNo, Optional<Long> categoryId,
                                                                   Optional<Long> subCategoryId, Optional<String> fromDateStr,
                                                                   Optional<String> toDateStr, Optional<String> status,
                                                                   Optional<Integer> page, Optional<Integer> size);
    Map<String, Object> getPurchaseOrderDetail(Long csId);

    void setVerificationAndApproval(Jwt token, String uri, Long csId);

    void reviewPo(Jwt token, Long id, NoteDto noteDto);

    void rejectPo(Jwt loggedInUser, Long id, NoteDto noteDto);

    void createPurchaseOrder(List<PurchaseOrder> purchaseOrders);

    void generatePurchaseOrder(Jwt token, String uri , PurchaseRequestDto purchaseRequestDto);
    void sentPoToVendors(PoGroup poGroup);
}
