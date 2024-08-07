package com.agi.aesl.erpscm.quality_control.service;

import com.agi.aesl.erpscm.quality_control.dto.request.QcDto;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface QcService extends VerificationDomainService {
    void addQc(Jwt token, QcDto qcDto) throws IllegalAccessException;

    List<?> getQcResultByGrn(Long id);

    void rejectQc(Long id);
}
