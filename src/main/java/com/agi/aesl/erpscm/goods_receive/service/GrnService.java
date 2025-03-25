package com.agi.aesl.erpscm.goods_receive.service;

import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualItemDetailDto;
import com.agi.aesl.erpscm.goods_receive.dto.request.GrnManualRequestDto;

import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveItemDetail;
import com.agi.aesl.erpscm.goods_receive.entity.GoodReceiveNote;
import com.agi.aesl.erpscm.goods_receive.enums.GrnMode;
import com.agi.aesl.erpscm.goods_receive.repository.GrnRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface GrnService {


    Page<GrnRepository.GoodReceiveNoteInfo> getAllGrn(Jwt token, Optional<Integer> page, Optional<Integer> size,
                                                      Optional<String> grnNo, Optional<Integer> qty,
                                                      Optional<Integer> receivedQty,
                                                      Optional<String> fromDate, Optional<String> toDate, Optional<String> grnStatus);

    <T> T getGrnById(Long id, Boolean returnTypeEntity);





    String createManualGrn(Jwt token, GrnManualRequestDto grnManualDto, GrnMode mode);
    void createAutoGrn(Jwt token, GrnManualRequestDto grnManualDto);


    Page<GrnRepository.GoodReceiveNoteInfo> getAllGrnPendingQC(Jwt token, Optional<Integer> page, Optional<Integer> size,
                               Optional<String> grnNo, Optional<Integer> qty,Optional<String> grnMode,Optional<String> poNo,
                               Optional<Integer> receivedQty,
                               Optional<String> fromDate, Optional<String> toDate);

    void updateGrnItemDetail(GoodReceiveItemDetail goodReceiveItemDetail);

    <T> T getGrn(Long id, boolean b);

    Optional<GoodReceiveNote> getByGrnNo(String srnNo);

    void receivedPO(Jwt token, Long id, List<GrnManualItemDetailDto> grnManualRequestDto);
    void declinePO(Jwt token,Long id, NoteDto noteDto);
}
