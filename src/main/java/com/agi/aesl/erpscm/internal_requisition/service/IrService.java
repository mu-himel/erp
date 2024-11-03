package com.agi.aesl.erpscm.internal_requisition.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.CreateIRDto;
import com.agi.aesl.erpscm.internal_requisition.dto.request.UpdateIRDetailDto;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface IrService extends VerificationDomainService {
    void createInternalRequisition(Jwt token, String uri, CreateIRDto createDto);

    Page<?> getAllInternalRequisitions(Optional<Integer> page, Optional<Integer> size,
                                       Optional<String> fromDate, Optional<String> toDate);

    String getNextIrNo();

    Page<?> getAllClosedIr(Optional<Integer> page, Optional<Integer> size,
                           Optional<String> fromDate, Optional<String> toDate);

    Page<?> getAllPendingVerificationIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                         Optional<String> fromDate, Optional<String> toDate);

    Page<?> getAllPendingApprovalIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                     Optional<String> fromDate, Optional<String> toDate);

    Page<?> getAllVerifiedOrApprovedIrs(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                        Optional<String> fromDate, Optional<String> toDate);

    Page<?> getAllProcessedIrs(Optional<Integer> page, Optional<Integer> size,
                               Optional<String> fromDate, Optional<String> toDate);

    <T> Optional<?> getDetail(Long id, Class<T> t);

    void reviewIr(Jwt token, Long id, ReviewDto reviewDto);

    void rejectIr(Jwt token, Long id, NoteDto noteDto);

    void updateIR(Jwt token, UpdateIRDetailDto updateIrDto);

    List<?> getWarehouses(Long id);
}
