package com.agi.aesl.erpscm.price_quotation.service;

import com.agi.aesl.erpscm.price_quotation.dto.request.PriceQuotationReqDto;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;


public interface PqService {
    void onReceivePq(Jwt token, PriceQuotationReqDto pqDto);

    void onDeclinePq(Long id, NoteDto noteDto, PriceQuotationStateStatus declined);

    List<?> getPriceQuotationsByIndent(Long id);

    Optional<?> getDetail(Long id);
}
