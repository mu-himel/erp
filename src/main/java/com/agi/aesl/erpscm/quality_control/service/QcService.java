package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.demand.dto.request.ReviewDto;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

public interface QcService extends VerificationDomainService {
    void addQc(Jwt token,String uri, QcDto qcDto) throws IllegalAccessException;

    Optional<?> getDetailByGrnId(Long id);

    Optional<?> getByGrnId(Long id);

    List<?> getQcResultByGrn(Long id);

    void rejectQc(Jwt token, Long id, NoteDto noteDto);

    void review(Jwt token, Long id, ReviewDto reviewDto);
}
