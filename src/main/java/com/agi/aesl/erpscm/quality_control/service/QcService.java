package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.goods_receive.service.GrnService;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.quality_control.repository.QcQuery;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QcService extends VerificationDomainService {

    void setGrnService(GrnService grnService);
    void addQc(Jwt token,String uri, QcDto qcDto) throws IllegalAccessException;

    Optional<Map<String,Object>> getDetailByGrnId(Long id);


    List<QcQuery.QcResultItem> getQcResultByGrn(Long id);

    void rejectQc(Jwt token, Long id, NoteDto noteDto);

    void review(Jwt token, Long id, ReviewDto reviewDto);

    Page<GrnRepository.GoodReceiveNoteInfo> getAllPendingVerificationQC(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate);

    Page<GrnRepository.GoodReceiveNoteInfo> getAllPendingApprovalQC(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate);

    Page<GrnRepository.GoodReceiveNoteInfo> getAllClosed(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate);

    Page<GrnRepository.GoodReceiveNoteInfo> getAllRejected(Jwt token, Optional<Integer> page, Optional<Integer> size, Optional<String> grnNo, Optional<Integer> qty, Optional<Integer> receivedQty, Optional<String> fromDate, Optional<String> toDate);
}
