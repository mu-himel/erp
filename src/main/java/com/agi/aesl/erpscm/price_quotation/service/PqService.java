package com.agi.aesl.erpscm.price_quotation.service;

import com.agi.aesl.erpscm.fileupload.dto.FileUploadResponse;
import com.agi.aesl.erpscm.price_quotation.dto.request.PriceQuotationReqDto;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import com.agi.aesl.erpscm.price_quotation.repository.PqRepository;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface PqService {
    record NegotiationHistory(Long id, String title){ }
    void onReceivePq(Jwt token, PriceQuotationReqDto pqDto);

    void onDeclinePq(Jwt token, Long id, NoteDto noteDto, PriceQuotationStateStatus declined, PriceQuotationStatus status);

    List<PqRepository.PriceQuotationInfo> getPriceQuotationsByIndent(Long id);

    Optional<Map<String,Object>> getDetail(Long id);

    List<NegotiationHistory> getHistoriesByRfq(Long id, Long vendorId);

    void lockPq(Jwt token, Long id, PriceQuotationStateStatus locked);

    void sendPq(Jwt token, PriceQuotationReqDto pqDto);


    void addManualPq(Jwt token, PriceQuotationReqDto pqDto);

    void recommendPq(Jwt token, Long id);

    void onLockPq(Jwt token, Long id);

    void onReceiveCounterPq(Jwt token, PriceQuotationReqDto pqDto);

    FileUploadResponse uploadDoc(Long id, MultipartFile file);
}
