package com.agi.aesl.erpscm.cs.service;

import com.agi.aesl.erpscm.cs.dto.AcsUpdateDto;
import com.agi.aesl.erpscm.cs.entity.Cs;
import com.agi.aesl.erpscm.cs.entity.CsAccount;
import com.agi.aesl.erpscm.quality_control.dto.request.NoteDto;
import com.agi.aesl.erpscm.user_application_validation.service.UserApplicationValidatorService;
import com.agi.aesl.erpscm.user_application_validation.service.VerificationDomainService;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface CsAccountService extends VerificationDomainService {
    void createCsAccount(Cs cs);

    void updateCsAccount(Long id, Jwt token, String uri, AcsUpdateDto acsUpdateDto);

    Page<?> getPendingAcs(Jwt token,
                          Optional<String> indenNo,Optional<String> status,
                          Optional<Integer> page, Optional<Integer> size);

    Page<?> getApprovedAcs(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);
    Page<?> getRejectedAcs(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);

    Page<?> getClosedAcs(Jwt token, Optional<String> indentNo, Optional<String> status, Optional<Integer> page, Optional<Integer> size);

    void reviewAcs(Jwt token, Long id, NoteDto noteDto);

    Page<?> getPendingVerificationAcs(Jwt token, Optional<String> indentNo,  Optional<Integer> page, Optional<Integer> size);

    Page<?> getPendingApprovalAcs(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);
    Page<?> getActiveCsList(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);

    Page<?> getExpiredCsList(Jwt token, Optional<String> indentNo, Optional<Integer> page, Optional<Integer> size);
}
