package com.agi.aesl.erpscm.purchase_order.service;

import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import javax.swing.text.html.Option;
import java.util.Map;
import java.util.Optional;

public interface PurchaseOrderService {
    Page<?> getPendingPOs(Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingVerificationPOs(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingApprovalPOs(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getApprovedPOs(Jwt token, Optional<Integer> page, Optional<Integer> size);

    Page<?> getClosedPOs(Jwt token, Optional<Integer> page, Optional<Integer> size);
    Map<String, Object> getPurchaseOrderDetail(Long csId);

    void setVerificationAndApproval(Jwt token, Long csId);

    void reviewPo(Jwt token, Long id, NoteDto noteDto);

    void rejectPo(Jwt loggedInUser, Long id, NoteDto noteDto);
}
